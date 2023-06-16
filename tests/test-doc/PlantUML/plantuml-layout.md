# PlantUML syntax examples

## Layout tailoring

@startuml
 
    ' change the arrow position to change direction
    A --> B
    C <-- D
    X *-- Y
    U --* V
 
    ' change the usual arrow length from two to one symbol (e.g. "-" instead of "--")
    ' to replace vertical edges with horizontal edges
    L *-- M
    L *- N
    O <. L
    L ..> P
 
    ' add layout hints (left, right, up, down) to let edges go
    ' up, down, from left to right and vice versa
    E -left-> F : left
    E o-right-> G : right
    E *-down-> H : down
    E .up.|> I : up

    footer Rendered with PlantUML version %version()
 
@enduml

@startuml
 
    class X
 
    ' change the arrow length by changing number of "-" or "." symbols
    X -> A
    X --> B
    X ---> C
    X ----> D
    X -----> E
    X ------> F

    footer Rendered with PlantUML version %version()
 
@enduml

@startuml
    ' without tailoring layout:
    class A
    class D
    class E
    class F
    A --> B
    A -> C
    C --> D

    footer Rendered with PlantUML version %version()
@enduml

@startuml
    ' with node groups and hidden edges:
    class D
 
    ' "together" defines a group of classes
    together {
        class A
        class E
        class F
    }
    A --> B
    A -> C
    C --> D
 
    ' a hidden edge from E to A forces position of class A
    E -[hidden]-> A

    footer Rendered with PlantUML version %version()
@enduml

@startuml
 
() "Product Dubbo" as product_dubbo
() "Member Dubbo" as member_dubbo
() "Guide Dubbo" as guide_dubbo
() "Search Dubbo" as search_dubbo
 
[ME]
[P] - product_dubbo
member_dubbo - [M]
[G] - guide_dubbo
search_dubbo - [S]
 
guide_dubbo <. [ME]
[ME] ..> member_dubbo
[ME] ..> product_dubbo
[ME] .> search_dubbo

    footer Rendered with PlantUML version %version()
 
@enduml

@startuml
 
skinparam fixCircleLabelOverlapping true
 
() "Product Dubbo" as product_dubbo
() "Member Dubbo" as member_dubbo
() "Guide Dubbo" as guide_dubbo
() "Search Dubbo" as search_dubbo
 
[ME]
[P] - product_dubbo
member_dubbo - [M]
[G] - guide_dubbo
search_dubbo - [S]
 
guide_dubbo <. [ME]
[ME] ..> member_dubbo
[ME] ..> product_dubbo
[ME] .> search_dubbo

footer Rendered with PlantUML version %version()
 
@enduml






