"use strict";(self.webpackChunkdocs=self.webpackChunkdocs||[]).push([[7019],{5115:(e,t,n)=>{n.r(t),n.d(t,{assets:()=>a,contentTitle:()=>o,default:()=>u,frontMatter:()=>i,metadata:()=>c,toc:()=>l});var r=n(4848),s=n(8453);const i={sidebar_position:3},o="Current HTTP Request",c={id:"framework/current-request",title:"Current HTTP Request",description:"Invirt provides several mechanisms for the application to access the current http4k Request object outside the",source:"@site/docs/framework/current-request.md",sourceDirName:"framework",slug:"/framework/current-request",permalink:"/docs/framework/current-request",draft:!1,unlisted:!1,tags:[],version:"current",sidebarPosition:3,frontMatter:{sidebar_position:3},sidebar:"frameworkSidebar",previous:{title:"Static Assets",permalink:"/docs/framework/static-assets"},next:{title:"Filters",permalink:"/docs/framework/filters"}},a={},l=[{value:"In Kotlin",id:"in-kotlin",level:2},{value:"In Pebble templates",id:"in-pebble-templates",level:2},{value:"InvirtRequest",id:"invirtrequest",level:2}];function d(e){const t={a:"a",code:"code",h1:"h1",h2:"h2",p:"p",pre:"pre",...(0,s.R)(),...e.components};return(0,r.jsxs)(r.Fragment,{children:[(0,r.jsx)(t.h1,{id:"current-http-request",children:"Current HTTP Request"}),"\n",(0,r.jsxs)(t.p,{children:["Invirt provides several mechanisms for the application to access the current http4k ",(0,r.jsx)(t.code,{children:"Request"})," object outside the\nhandler. This is useful for several scenarios, including the rendering of templates that require access to the\ncurrent request or URI."]}),"\n",(0,r.jsx)(t.h2,{id:"in-kotlin",children:"In Kotlin"}),"\n",(0,r.jsxs)(t.p,{children:["Invirt automatically stores the current http4k request in the ",(0,r.jsx)(t.a,{href:"/docs/api/invirt-core/request-context",children:"InvirtRequestContext"}),",\nwhich in turn exposes a readonly ",(0,r.jsx)(t.code,{children:"request"})," property that can be used to access the request anywhere within the application."]}),"\n",(0,r.jsx)(t.pre,{children:(0,r.jsx)(t.code,{className:"language-kotlin",children:"println(InvirtRequestContext.request!!.uri)\n"})}),"\n",(0,r.jsx)(t.h2,{id:"in-pebble-templates",children:"In Pebble templates"}),"\n",(0,r.jsxs)(t.p,{children:["Pebble templates can access the request object directly from the ",(0,r.jsx)(t.a,{href:"/docs/api/invirt-core/pebble-context-objects#request",children:(0,r.jsx)(t.code,{children:"request"})}),"\nobject in the root context, when not inside a macro. Inside macros, the ",(0,r.jsx)(t.a,{href:"/docs/api/invirt-core/pebble-functions#request",children:(0,r.jsx)(t.code,{children:"request()"})}),"\nPebble function must be used instead (due to the fact that ",(0,r.jsx)(t.a,{href:"https://pebbletemplates.io/wiki/tag/macro/",children:"macros don't have access to the global context"}),")."]}),"\n",(0,r.jsx)(t.pre,{children:(0,r.jsx)(t.code,{className:"language-html",children:'{% macro requestSummary() %}\n    {{ request().uri }}\n    {{ request().method }}\n{% endmacro %}\n\n{{ request.method }}\n{{ request.query("q") }}\n'})}),"\n",(0,r.jsx)(t.h2,{id:"invirtrequest",children:"InvirtRequest"}),"\n",(0,r.jsxs)(t.p,{children:["Invirt wraps the core http4k ",(0,r.jsx)(t.code,{children:"Request"})," object in an ",(0,r.jsx)(t.code,{children:"InvirtRequest"}),", which implements http4k's ",(0,r.jsx)(t.a,{href:"https://www.http4k.org/api/org.http4k.core/-request/",children:"Request interface"}),".\n",(0,r.jsx)(t.code,{children:"InvirtRequest"})," delegates to the native http4k ",(0,r.jsx)(t.code,{children:"Request"})," for all interface operations, and adds a set of functions for wiring\n",(0,r.jsx)(t.a,{href:"/docs/api/invirt-core/uri-extensions",children:"URI extensions"}),"."]}),"\n",(0,r.jsxs)(t.p,{children:["You don't typically need access to the ",(0,r.jsx)(t.code,{children:"InvirtRequest"})," object from your applications Kotlin code. However, in a Pebble template,\nthis is required in order to provide access to above-mentioned extensions, as Pebble can't operate Kotlin extension functions directly."]}),"\n",(0,r.jsx)(t.pre,{children:(0,r.jsx)(t.code,{className:"language-html",children:"{% if request.hasQueryValue('type', 'person') %}\n    <a href=\"{{ request.replaceQuery('type', 'company') }}\">\n        Show companies\n    </a>\n{% endif %}\n"})})]})}function u(e={}){const{wrapper:t}={...(0,s.R)(),...e.components};return t?(0,r.jsx)(t,{...e,children:(0,r.jsx)(d,{...e})}):d(e)}},8453:(e,t,n)=>{n.d(t,{R:()=>o,x:()=>c});var r=n(6540);const s={},i=r.createContext(s);function o(e){const t=r.useContext(i);return r.useMemo((function(){return"function"==typeof e?e(t):{...t,...e}}),[t,e])}function c(e){let t;return t=e.disableParentContext?"function"==typeof e.components?e.components(s):e.components||s:o(e.components),r.createElement(i.Provider,{value:t},e.children)}}}]);