"use strict";(self.webpackChunkdocs=self.webpackChunkdocs||[]).push([[8437],{8314:(e,t,n)=>{n.r(t),n.d(t,{assets:()=>l,contentTitle:()=>r,default:()=>u,frontMatter:()=>a,metadata:()=>c,toc:()=>d});var i=n(4848),s=n(8453),o=n(7301);const a={sidebar_position:1},r="Overview",c={id:"framework/security/overview",title:"Overview",description:"Invirt's security module focuses exclusively on authentication and it provides",source:"@site/docs/framework/security/overview.md",sourceDirName:"framework/security",slug:"/framework/security/overview",permalink:"/docs/framework/security/overview",draft:!1,unlisted:!1,tags:[],version:"current",sidebarPosition:1,frontMatter:{sidebar_position:1},sidebar:"frameworkSidebar",previous:{title:"Example application",permalink:"/docs/framework/data-querying/example"},next:{title:"Core concepts",permalink:"/docs/framework/security/core-concepts"}},l={},d=[{value:"Dependency",id:"dependency",level:2},{value:"Use case",id:"use-case",level:2},{value:"What Invirt Security doesn&#39;t do",id:"what-invirt-security-doesnt-do",level:2},{value:"Login/Logout",id:"loginlogout",level:4},{value:"Authorisation",id:"authorisation",level:4},{value:"Path-based access control",id:"path-based-access-control",level:4}];function h(e){const t={a:"a",code:"code",h1:"h1",h2:"h2",h4:"h4",p:"p",pre:"pre",...(0,s.R)(),...e.components};return(0,i.jsxs)(i.Fragment,{children:[(0,i.jsx)(t.h1,{id:"overview",children:"Overview"}),"\n",(0,i.jsxs)(t.p,{children:["Invirt's security module focuses exclusively on authentication and it provides\na set of components for transparently authenticating HTTP requests via a custom ",(0,i.jsx)(t.a,{href:"https://www.http4k.org/guide/reference/core/#filters",children:"http4k filter"}),"."]}),"\n",(0,i.jsx)(t.h2,{id:"dependency",children:"Dependency"}),"\n",(0,i.jsx)(t.pre,{children:(0,i.jsx)(t.code,{className:"language-kotlin",children:'implementation(platform("io.resoluteworks:invirt-bom:${invirtVersion}"))\nimplementation("io.resoluteworks:invirt-security")\n'})}),"\n",(0,i.jsx)(t.h2,{id:"use-case",children:"Use case"}),"\n",(0,i.jsx)(t.p,{children:"The first problem we aim to solve is to provide an application with context for the currently\nauthenticated user (Principal) transparently, and allowing for that to be checked anywhere within the stack.\nWe use a ThreadLocal for this purpose, and request context."}),"\n",(0,i.jsx)(t.p,{children:"Second, we want to allow the application to decide what authentication solution it wants to use, with Invirt simply\nproviding the scaffolding to wire that in, and secure certain application routes."}),"\n",(0,i.jsx)(t.p,{children:"Lastly, we wanted to make the tooling as un-intrusive as possible, and allow the application to define\nthe concepts of user or principal according to its requirements, without heavy constraints from the framework\non how these must be implemented and handled."}),"\n",(0,i.jsxs)(t.p,{children:["Below is a high level view of a happy flow for authenticating a request using request cookies.\nWe discuss ",(0,i.jsx)(t.a,{href:"/docs/framework/security/core-concepts",children:"these concepts"})," in detail and\nthere is also an ",(0,i.jsx)(t.a,{href:"/docs/framework/security/example",children:"example application"})," to explore them."]}),"\n",(0,i.jsx)("img",{src:o.A}),"\n",(0,i.jsx)(t.h2,{id:"what-invirt-security-doesnt-do",children:"What Invirt Security doesn't do"}),"\n",(0,i.jsx)(t.h4,{id:"loginlogout",children:"Login/Logout"}),"\n",(0,i.jsx)(t.p,{children:"As these operations are usually heavy coupled to the authentication provider being used and the application\ndesign, we left this to the developer to wire according to the system requirements."}),"\n",(0,i.jsx)(t.h4,{id:"authorisation",children:"Authorisation"}),"\n",(0,i.jsx)(t.p,{children:"Invirt doesn't implement authorisation semantics, as we felt that this is an area where the application\nmust be allowed flexibility. We didn't want to make any assumptions about the applications authorisation requirements\nand whether it should use RBAC (Role Based Access Control) or ABAC (Attribute-Based Access), for example."}),"\n",(0,i.jsx)(t.h4,{id:"path-based-access-control",children:"Path-based access control"}),"\n",(0,i.jsxs)(t.p,{children:["Some frameworks provide utilities to define paths and regular expressions to secure certain routes and\nresources based on a Principal's role or attributes. For example ",(0,i.jsx)(t.code,{children:"/admin/*"})," can only be accessed by Role.ADMIN, etc.\nThis is a practice that has a lot of limitations and it leads to a code base that is hard to maintain.\nIt also falls in the realm of authorisation, which we discussed above."]}),"\n",(0,i.jsx)(t.p,{children:"That being said, should you require something along these lines, Invirt does provide a basic utility to wire\ncustom Principal checks via a filter in a functional style."}),"\n",(0,i.jsx)(t.pre,{children:(0,i.jsx)(t.code,{className:"language-kotlin",children:'val permissionChecker: (Principal) -> Boolean = { principal ->\n    "ADMIN" in principal.roles\n}\n\nval handler = securedRoutes(\n    permissionChecker,\n    routes(\n        "/admin" GET { Response(Status.OK) },\n        "/admin/test" GET { Response(Status.OK) }\n    )\n)\n'})})]})}function u(e={}){const{wrapper:t}={...(0,s.R)(),...e.components};return t?(0,i.jsx)(t,{...e,children:(0,i.jsx)(h,{...e})}):h(e)}},7301:(e,t,n)=>{n.d(t,{A:()=>i});const i=n.p+"assets/images/authentication-happy-flow-4e143881653d97ac9b469800b224b685.png"},8453:(e,t,n)=>{n.d(t,{R:()=>a,x:()=>r});var i=n(6540);const s={},o=i.createContext(s);function a(e){const t=i.useContext(o);return i.useMemo((function(){return"function"==typeof e?e(t):{...t,...e}}),[t,e])}function r(e){let t;return t=e.disableParentContext?"function"==typeof e.components?e.components(s):e.components||s:a(e.components),i.createElement(o.Provider,{value:t},e.children)}}}]);