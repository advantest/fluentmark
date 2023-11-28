# FluentMark Advantest Edition (AE)

## Context


@startuml

   'skinparam fixCircleLabelOverlapping true
   
   !define EXTERNAL <<external>> #lightblue

    actor Developer
    [FluentMark] #lightgray
    [Eclipse] EXTERNAL
    [pandoc] EXTERNAL
    [PlantUML] EXTERNAL
    [GraphViz] EXTERNAL

    [Eclipse] -r- () "extensions points"
    [FluentMark] .l.> "extensions points"
    [pandoc] -l- () CLI
    [FluentMark] .d.> CLI
    [PlantUML] -l- () API
    [FluentMark] .r.> API
    [PlantUML] -[hidden]d-pandoc
    [PlantUML] .r.> [GraphViz]
    Developer ..> [FluentMark]: uses

footer Rendered with PlantUML version %version()
@enduml


	

## Building Blocks

@startuml

   'skinparam fixCircleLabelOverlapping true
   
   !define EXTERNAL <<external>> #lightblue

    [pandoc] EXTERNAL
    [PlantUML] EXTERNAL

    component "FluentMark AE" #lightgray {
        component net.certiv.fluentmark.core as core
        component net.certiv.fluentmark.ui as ui
        component net.certiv.spellchecker as spellchecker
        component net.certiv.tools as tools
        component com.advantest.fluentmark.extensions.ui as extUI
        component com.advantest.fluentmark.extensions.svgbuilder as svgbuilder
        component com.advantest.fluentmark.extensions.validations as validations
    }

    [ui] .u.> [core]
    [ui] .u.> [spellchecker]
    [ui] .u.> [tools]
    [extUI] .u.> [core]
    [extUI] .u.> [ui]
    [svgbuilder] .u.> [core]
    [svgbuilder] .u.> [ui]
    [svgbuilder] -[hidden]r- [extUI]
    [validations] .u.> [core]
    [validations] .u.> [ui]
    [validations] .u.> [svgbuilder]
    [core] .r.> [PlantUML]
    [core] ..> [pandoc]
    
@enduml



## Core Plug-in


@startuml

set separator none
hide empty members

!define EXTERNAL <<external>> #lightblue

interface IResource EXTERNAL

'component net.certiv.fluentmark.core as core #lightgray {

    package net.certiv.fluentmark.core.convert #FFFFFF {
        class HtmlGen {
            buildHtml(IPath,... IDocument,...): String
        }
        class Converter {
            convert(IPath,... IDocument,...): String
            getText(IPath, IDocument, ITypedRegion[],...): String
        }
        note right of Converter::convert
            uses pandoc to translate pre-processed
            Markdown code to HTML
        end note
        note right of Converter::getText
            does some pre-processing
        end note
        class UmlGen {
            uml2svg(String): String
        }
        note right of UmlGen::uml2svg
            calls PlantUML API
        end note
        'class DotGen
    }
    package net.certiv.fluentmark.core.markdown #FFFFFF {
        class PageRoot <<Singleton>> {
            updateModel(...)
        }
        class PagePart
        class Lines
        class "Lines.Line" as Line
        abstract class Parent
        class UpdateJob {
            run(...): IStatus
        }
        class "UpdateJob.Task" as Task
        note bottom of Task
            parses a Markdown file
            and re-calculates the model
        end note
    }
    
'}

Converter --> "1" UmlGen
HtmlGen --> "1" Converter 
'Converter --> "1" DotGen

PageRoot -u-|> Parent
PagePart -u-|> Parent
PageRoot "root" *-d- "*" PagePart : parts
PageRoot *-d-> "0..1" Lines : lines
PagePart *--> "n" Line
Lines [lineNum:int] --> "1" Line
UpdateJob *--> "m" Task
Task -> "1" PageRoot
Parent *--> "children" Parent
Task -d-> "1" IResource

@enduml


	
