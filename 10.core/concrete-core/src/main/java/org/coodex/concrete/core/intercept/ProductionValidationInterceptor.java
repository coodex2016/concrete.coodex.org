/*
 * Copyright (c) 2019 coodex.org (jujus.shen@126.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.coodex.concrete.core.intercept;

import org.aopalliance.intercept.MethodInvocation;
import org.coodex.concrete.api.Modules;
import org.coodex.concrete.common.*;
import org.coodex.concrete.core.intercept.annotations.ServerSide;
import org.coodex.concrete.core.token.TokenWrapper;
import org.coodex.util.AcceptableServiceLoader;
import org.coodex.util.Clock;
import org.coodex.util.Common;

import java.util.Calendar;
import java.util.List;

import static org.coodex.concrete.common.ErrorCodes.PRODUCTION_NONE_THIS_MODULE;
import static org.coodex.concrete.common.ErrorCodes.PRODUCTION_OVERDUE;
import static org.coodex.concrete.core.intercept.InterceptOrders.PRODUCTION_CHECK;

@ServerSide
public class ProductionValidationInterceptor extends AbstractSyncInterceptor {

    private Token token = TokenWrapper.getInstance();
    private AcceptableServiceLoader<Account, ProductionRepository> productionRepositoryAcceptableServiceLoader =
            new AcceptableServiceLoader<Account, ProductionRepository>() {
            };

    @Override
    protected boolean accept_(DefinitionContext context) {
        return context.getAnnotation(Modules.NonFunctional.class) == null &&
                context.getAnnotation(Modules.class) != null;
    }

    @Override
    public int getOrder() {
        return PRODUCTION_CHECK;
    }

    @Override
    public Object around(DefinitionContext context, MethodInvocation joinPoint) throws Throwable {
        Account account = token.currentAccount();
        if (account != null) {
            productionCheck(context, account);
        }
        try {
            Object result = super.around(context, joinPoint);
            if (account == null) {
                account = token.currentAccount();
                if (account != null) {
                    productionCheck(context, account);
                }
            }
            return result;
        } catch (ConcreteException ce) {
            if (ce.getCode() == PRODUCTION_OVERDUE || ce.getCode() == PRODUCTION_NONE_THIS_MODULE) {
                token.invalidate();
            }
            throw ce;
        }
    }

    private Production findIn(List<Production> productions, String[] modules) {
        Production result = null;
        for (Production production : productions) {
            if (production.getModules() != null && production.getModules().size() > 0) {
                for (String module : modules) {
                    if (production.getModules().contains(module)) {
                        if (production.getCalendar() == null) return production;
                        if (result == null) {
                            result = production;
                        } else {
                            if (production.getCalendar().after(result.getCalendar())) {
                                result = production;
                            }
                        }
                    }
                }
            }
        }
        return result;
    }

    private void productionCheck(DefinitionContext context, Account account) {
        ProductionRepository productionRepository = productionRepositoryAcceptableServiceLoader.select(account);
        if (productionRepository == null) return;

        Modules modules = context.getAnnotation(Modules.class);
        List<Production> productions = productionRepository.getProductionsBy(account, modules.values());
        if (productions == null || productions.size() == 0) throw new ConcreteException(PRODUCTION_NONE_THIS_MODULE);

        Production production = findIn(productions, modules.values());
        if (production == null) throw new ConcreteException(PRODUCTION_NONE_THIS_MODULE);

        Calendar overRun = production.getCalendar();
        if (overRun == null) return;

        Calendar now = Clock.getCalendar();

        if (now.after(overRun)) {
            throw new ConcreteException(PRODUCTION_OVERDUE, production.getProductionName());
        }

        if (production.getRemindDays() > 0) {
            now.add(Calendar.DATE, production.getRemindDays());
            if (now.after(overRun)) {
                SubjoinWrapper.getInstance().putWarning(new WarningData(
                        ErrorCodes.PRODUCTION_OVERDUE_REMIND,
                        production.getProductionName(),
                        Common.calendarToStr(production.getCalendar(), Common.DEFAULT_DATE_FORMAT)
                ));
            }
        }
    }
}
