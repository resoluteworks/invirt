"use strict";(self.webpackChunkdocs=self.webpackChunkdocs||[]).push([[8815],{1368:(e,n,i)=>{i.r(n),i.d(n,{assets:()=>l,contentTitle:()=>a,default:()=>p,frontMatter:()=>s,metadata:()=>o,toc:()=>c});var t=i(4848),r=i(8453);const s={sidebar_position:2},a="Quick Start",o={id:"overview/quickstart",title:"Quick Start",description:"For in-depth documentation please check:",source:"@site/docs/overview/quickstart.md",sourceDirName:"overview",slug:"/overview/quickstart",permalink:"/docs/overview/quickstart",draft:!1,unlisted:!1,tags:[],version:"current",sidebarPosition:2,frontMatter:{sidebar_position:2},sidebar:"overviewSidebar",previous:{title:"Introduction",permalink:"/docs/overview/introduction"},next:{title:"Running examples",permalink:"/docs/overview/examples"}},l={},c=[{value:"Quick Start Application",id:"quick-start-application",level:2},{value:"Dependencies",id:"dependencies",level:3},{value:"Project structure",id:"project-structure",level:3},{value:"Application",id:"application",level:3},{value:"Wiring explained",id:"wiring-explained",level:3},{value:"1. Initialising Invirt views",id:"1-initialising-invirt-views",level:4},{value:"2. InvirtFilter",id:"2-invirtfilter",level:4}];function d(e){const n={a:"a",code:"code",h1:"h1",h2:"h2",h3:"h3",h4:"h4",li:"li",p:"p",pre:"pre",ul:"ul",...(0,r.R)(),...e.components};return(0,t.jsxs)(t.Fragment,{children:[(0,t.jsx)(n.h1,{id:"quick-start",children:"Quick Start"}),"\n",(0,t.jsx)(n.p,{children:"For in-depth documentation please check:"}),"\n",(0,t.jsxs)(n.ul,{children:["\n",(0,t.jsx)(n.li,{children:(0,t.jsx)(n.a,{href:"/docs/framework/views-wiring",children:"Framework documentation"})}),"\n",(0,t.jsx)(n.li,{children:(0,t.jsx)(n.a,{href:"/docs/api/invirt-core/route-binding",children:"API documentation"})}),"\n"]}),"\n",(0,t.jsx)(n.h2,{id:"quick-start-application",children:"Quick Start Application"}),"\n",(0,t.jsx)(n.p,{children:(0,t.jsx)(n.a,{href:"https://github.com/resoluteworks/invirt/tree/main/examples/quickstart",children:"Example application"})}),"\n",(0,t.jsx)(n.h3,{id:"dependencies",children:"Dependencies"}),"\n",(0,t.jsx)(n.p,{children:"Invirt comes as a set of libraries, discussed later in this documentation, and which can be added incrementally\nas you expand your application's design. Most of the functionality, however, is contained\nin the core library which can be added as per Gradle example below."}),"\n",(0,t.jsx)(n.pre,{children:(0,t.jsx)(n.code,{className:"language-kotlin",children:'implementation(platform("io.resoluteworks:invirt-bom:${invirtVersion}"))\nimplementation("io.resoluteworks:invirt-core")\n'})}),"\n",(0,t.jsxs)(n.p,{children:["You will also need to add the http4k libraries which Invirt relies on. Below is the minimum required\nto get started with an Invirt app. Netty is simply used as an example, you can of course choose your\npreferred ",(0,t.jsx)(n.a,{href:"https://www.http4k.org/guide/reference/servers/",children:"http4k server backend"}),"."]}),"\n",(0,t.jsx)(n.pre,{children:(0,t.jsx)(n.code,{className:"language-kotlin",children:'implementation(platform("org.http4k:http4k-bom:${http4kVersion}"))\nimplementation("org.http4k:http4k-core")\nimplementation("org.http4k:http4k-server-netty")\nimplementation("org.http4k:http4k-cloudnative")\nimplementation("org.http4k:http4k-template-pebble")\n'})}),"\n",(0,t.jsx)(n.h3,{id:"project-structure",children:"Project structure"}),"\n",(0,t.jsxs)(n.p,{children:["The structure of an Invirt project is similar to any other http4k application, with some built-in defaults\nfor template look-ups. For a complete example, please check the ",(0,t.jsx)(n.a,{href:"https://github.com/resoluteworks/invirt/tree/main/examples/quickstart",children:"Quickstart project"}),"."]}),"\n",(0,t.jsx)(n.pre,{children:(0,t.jsx)(n.code,{className:"language-text",children:"\u251c\u2500\u2500 build.gradle.kts\n\u2514\u2500\u2500 src\n    \u2514\u2500\u2500 main\n        \u251c\u2500\u2500 kotlin\n        \u2502   \u2514\u2500\u2500 examples\n        \u2502       \u2514\u2500\u2500 quickstart\n        \u2502           \u2514\u2500\u2500 Application.kt\n        \u2514\u2500\u2500 resources\n            \u2514\u2500\u2500 webapp\n                \u2514\u2500\u2500 views\n                    \u2514\u2500\u2500 index.peb\n"})}),"\n",(0,t.jsx)(n.h3,{id:"application",children:"Application"}),"\n",(0,t.jsx)(n.pre,{children:(0,t.jsx)(n.code,{className:"language-kotlin",children:'class IndexResponse(val currentUsername: String) : ViewResponse("index.peb")\n\nclass Application {\n\n    fun start() {\n        initialiseInvirtViews()\n\n        val appHandler = InvirtFilter().then(\n            routes(\n                "/" GET {\n                    IndexResponse(currentUsername = "email@test.com").ok()\n                }\n            )\n        )\n\n        val server = Netty(8080)\n        server.toServer(appHandler).start()\n    }\n}\n'})}),"\n",(0,t.jsxs)(n.p,{children:[(0,t.jsx)(n.code,{children:"IndexResponse"})," extends Invirt's ",(0,t.jsx)(n.code,{children:"ViewResponse"})," (a convenience implementation of http4k's ",(0,t.jsx)(n.a,{href:"https://www.http4k.org/api/org.http4k.template/-view-model/",children:"ViewModel"}),").\nThis object stores the data to be used in the template (",(0,t.jsx)(n.code,{children:"currentUsername"}),"), and defines the template to be rendered (",(0,t.jsx)(n.code,{children:"index.peb"}),")"]}),"\n",(0,t.jsxs)(n.p,{children:[(0,t.jsx)(n.code,{children:"IndexResponse"})," is available as the ",(0,t.jsx)(n.code,{children:"model"})," object within the template, as per example below."]}),"\n",(0,t.jsx)(n.pre,{children:(0,t.jsx)(n.code,{className:"language-html",children:"<div>\n    Current user is {{ model.currentUsername }}\n</div>\n"})}),"\n",(0,t.jsx)(n.h3,{id:"wiring-explained",children:"Wiring explained"}),"\n",(0,t.jsx)(n.p,{children:"In the code above there are two components required to enable Invirt in your http4k application."}),"\n",(0,t.jsx)(n.h4,{id:"1-initialising-invirt-views",children:"1. Initialising Invirt views"}),"\n",(0,t.jsx)(n.pre,{children:(0,t.jsx)(n.code,{className:"language-kotlin",children:"initialiseInvirtViews()\n"})}),"\n",(0,t.jsxs)(n.p,{children:["This sets a default view lens to be used throughout your application when rendering Pebble template responses.\nWe recommend reading more about http4k's ",(0,t.jsx)(n.a,{href:"https://www.http4k.org/guide/howto/use_a_templating_engine/",children:"templating capabilities"}),", most of Invirt\nis built on top of those."]}),"\n",(0,t.jsxs)(n.p,{children:["There are several parameters that can be passed to ",(0,t.jsx)(n.code,{children:"initialiseInvirtViews()"})," to override the default behaviour.\nAll of these are discussed in detail in ",(0,t.jsx)(n.a,{href:"/docs/framework/views-wiring",children:"Pebble Views Wiring"}),"."]}),"\n",(0,t.jsx)(n.h4,{id:"2-invirtfilter",children:"2. InvirtFilter"}),"\n",(0,t.jsx)(n.pre,{children:(0,t.jsx)(n.code,{className:"language-kotlin",children:"val appHandler = InvirtFilter()\n    .then(routes(...))\n"})}),"\n",(0,t.jsxs)(n.p,{children:[(0,t.jsx)(n.code,{children:"InvirtFilter"})," handles a few of the framework's internals, including setting the current http4k ",(0,t.jsx)(n.code,{children:"Request"}),"\non the current thread, as well as managing validation errors for a request. These are in turn exposed internally\nto other Invirt components and your application. You can add this filter anywhere in your application's filter chain\nbefore wiring your http4k routes."]})]})}function p(e={}){const{wrapper:n}={...(0,r.R)(),...e.components};return n?(0,t.jsx)(n,{...e,children:(0,t.jsx)(d,{...e})}):d(e)}},8453:(e,n,i)=>{i.d(n,{R:()=>a,x:()=>o});var t=i(6540);const r={},s=t.createContext(r);function a(e){const n=t.useContext(s);return t.useMemo((function(){return"function"==typeof e?e(n):{...n,...e}}),[n,e])}function o(e){let n;return n=e.disableParentContext?"function"==typeof e.components?e.components(r):e.components||r:a(e.components),t.createElement(s.Provider,{value:n},e.children)}}}]);