"use strict";(self.webpackChunkdocs=self.webpackChunkdocs||[]).push([[6050],{7951:(e,t,n)=>{n.r(t),n.d(t,{assets:()=>l,contentTitle:()=>o,default:()=>p,frontMatter:()=>r,metadata:()=>s,toc:()=>d});var i=n(4848),a=n(8453);const r={sidebar_position:1},o="Overview",s={id:"framework/data-querying/overview",title:"Overview",description:"Invirt provides the wiring for an application to derive filtering, pagination and sorting logic",source:"@site/docs/framework/data-querying/overview.md",sourceDirName:"framework/data-querying",slug:"/framework/data-querying/overview",permalink:"/docs/framework/data-querying/overview",draft:!1,unlisted:!1,tags:[],version:"current",sidebarPosition:1,frontMatter:{sidebar_position:1},sidebar:"frameworkSidebar",previous:{title:"Form validation",permalink:"/docs/framework/forms/form-validation"},next:{title:"Example application",permalink:"/docs/framework/data-querying/example"}},l={},d=[{value:"Dependency",id:"dependency",level:2},{value:"Rationale",id:"rationale",level:2}];function c(e){const t={a:"a",admonition:"admonition",code:"code",em:"em",h1:"h1",h2:"h2",mdxAdmonitionTitle:"mdxAdmonitionTitle",p:"p",pre:"pre",...(0,a.R)(),...e.components};return(0,i.jsxs)(i.Fragment,{children:[(0,i.jsx)(t.h1,{id:"overview",children:"Overview"}),"\n",(0,i.jsx)(t.admonition,{type:"note",children:(0,i.jsx)(t.mdxAdmonitionTitle,{children:(0,i.jsx)(t.a,{href:"https://github.com/resoluteworks/invirt/tree/main/examples/data-querying",children:"Example application"})})}),"\n",(0,i.jsx)(t.p,{children:"Invirt provides the wiring for an application to derive filtering, pagination and sorting logic\nfrom a request's query parameters. A set of abstractions for these components are define\nby invirt-data, a small library that you can add to your application to leverage these\ncapabilities."}),"\n",(0,i.jsx)(t.h2,{id:"dependency",children:"Dependency"}),"\n",(0,i.jsx)(t.pre,{children:(0,i.jsx)(t.code,{className:"language-kotlin",children:'implementation(platform("dev.invirt:invirt-bom:${invirtVersion}"))\nimplementation("dev.invirt:invirt-data")\n'})}),"\n",(0,i.jsx)(t.h2,{id:"rationale",children:"Rationale"}),"\n",(0,i.jsxs)(t.p,{children:["It's important to note that Invirt doesn't provide implementations of ",(0,i.jsx)(t.a,{href:"/docs/api/invirt-data/data-filter",children:"these abstractions"}),"\nfor specific databases, and that would be out of scope for the foreseeable future."]}),"\n",(0,i.jsx)(t.p,{children:"The main reason for this is because we wanted to provide greater flexibility of query building,\nand a decoupling of DB querying logic from the presentation layer. Mapping URL query parameters\ndirectly to database fields, is not always the right approach to achieve that."}),"\n",(0,i.jsxs)(t.p,{children:["In some cases, the absence of a query parameter means the system would apply an implicit filter.\nFor example, a website listing properties for sale, by default would only list\nproperties that are still available: ",(0,i.jsx)(t.code,{children:"sold == false"}),". It would offer the user an option to include sold ones\nvia ",(0,i.jsx)(t.code,{children:"&include-sold=true"}),", but the presence of that query parameter would imply the ",(0,i.jsx)(t.em,{children:"absence"})," of the\n",(0,i.jsx)(t.code,{children:"sold == false"})," DB filter."]}),"\n",(0,i.jsxs)(t.p,{children:["Another example are activities that have a start date and a deadline, like\napplying to take part in a competition. A user-friendly filter here might be \"Show open competitions\", indicating\nevents that have officially open the application process, but haven't reached the deadline date yet (they're still\ntaking applications). In this case, the system might want to provide a compound filter for a ",(0,i.jsx)(t.code,{children:"&status=open"})," query parameter:"]}),"\n",(0,i.jsx)(t.pre,{children:(0,i.jsx)(t.code,{className:"language-sql",children:"(openingDate >= TODAY) AND (TODAY < deadline)\n"})}),"\n",(0,i.jsx)(t.p,{children:"While all of these have workarounds and alternatives, all of them invariably end up coupling the user interface\nto the underlying model of the database. It's this separation of concerns that we aim to solve with Invirt and the\nabstractions we've defined."})]})}function p(e={}){const{wrapper:t}={...(0,a.R)(),...e.components};return t?(0,i.jsx)(t,{...e,children:(0,i.jsx)(c,{...e})}):c(e)}},8453:(e,t,n)=>{n.d(t,{R:()=>o,x:()=>s});var i=n(6540);const a={},r=i.createContext(a);function o(e){const t=i.useContext(r);return i.useMemo((function(){return"function"==typeof e?e(t):{...t,...e}}),[t,e])}function s(e){let t;return t=e.disableParentContext?"function"==typeof e.components?e.components(a):e.components||a:o(e.components),i.createElement(r.Provider,{value:t},e.children)}}}]);