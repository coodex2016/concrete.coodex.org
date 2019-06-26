# concrete for product

## 产品体系

- product.module.service
    - product: 产品。一个产品可以由多个模块构成
    - module: 模块。一个模块可由多个服务构成，由服务声明；一个模块也可以属于多个产品
    - service: ConcreteService
- 每个product交付客户方时，不管是云模式还是本地部署模式，实质上是一个product instance，需要产生一个productInstanceKey，客户方使用产品内服务时，须要带productInstanceKey。
- product instance可以自行定义产品内各模块的限制，例如：产品A由M1, M2构成，产品包含两个实例A1, A2，可以分别指定A1.ALL/ 
A1.M1/ A1.M2/ A2.ALL/ A2.M1/ A2.M2的使用限制（时限、次数等，可扩展），同时，可以分产品实例指定冲突解决方案，例如，有一个服务
同时属于M1和M2，A1的冲突策略指定为就短原则，A2的冲突策略指定为就长原则
- 回过来看，几乎就是SaaS的计费模型，完美

## usage

```java
@ConcreteService
@Modules({"M1", "M2"}) // 此服务属于M1和M2
public interface SomeService{
    // ....
}
```
