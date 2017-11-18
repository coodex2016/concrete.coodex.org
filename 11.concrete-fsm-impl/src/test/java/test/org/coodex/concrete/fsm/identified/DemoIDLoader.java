package test.org.coodex.concrete.fsm.identified;

import org.coodex.concrete.fsm.IdentifiedStateLoader;
import org.coodex.util.Common;

import java.util.HashMap;
import java.util.Map;

public class DemoIDLoader implements IdentifiedStateLoader<DemoIdState, String> {

    private Map<String, DemoIdState> store = new HashMap<String, DemoIdState>();

    @Override
    public DemoIdState newState() {
        String id = Common.getUUIDStr();
        DemoIdState idState = new DemoIdState();
        idState.setId(id);
        store.put(id, idState);
        return idState;
    }

    @Override
    public DemoIdState getState(String s) {
        return store.get(s);
    }
}
