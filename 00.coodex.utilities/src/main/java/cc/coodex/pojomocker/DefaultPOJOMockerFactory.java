/**
 * 
 */
package cc.coodex.pojomocker;


/**
 * @author davidoff
 *
 */
public class DefaultPOJOMockerFactory extends POJOMockerFactory {

   /**
    * 注册String/基础类型/数组类型/Collection/List/Set的支持
    */
   public DefaultPOJOMockerFactory() {
      registMocker(new CI_StringMocker());
      registMocker(new CI_PrivimitiveTypeMocker());
      registMocker(new CI_ArrayMocker());
      registMocker(new CI_ListMocker());
      registMocker(new CI_SetMocker());
      registMocker(new CI_MapMocker());
   }

}
