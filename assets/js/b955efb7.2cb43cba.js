"use strict";(self.webpackChunkdocs=self.webpackChunkdocs||[]).push([[1638],{5806:(e,s,t)=>{t.r(s),t.d(s,{assets:()=>c,contentTitle:()=>i,default:()=>d,frontMatter:()=>o,metadata:()=>a,toc:()=>l});var n=t(4848),r=t(8453);const o={sidebar_position:1},i="ViewResponse",a={id:"framework/views-response",title:"ViewResponse",description:"ViewResponse implements the ViewModel interface",source:"@site/docs/framework/views-response.md",sourceDirName:"framework",slug:"/framework/views-response",permalink:"/docs/framework/views-response",draft:!1,unlisted:!1,tags:[],version:"current",sidebarPosition:1,frontMatter:{sidebar_position:1},sidebar:"frameworkSidebar",previous:{title:"Configuration",permalink:"/docs/framework/configuration"},next:{title:"Static Assets",permalink:"/docs/framework/static-assets"}},c={},l=[];function p(e){const s={a:"a",code:"code",h1:"h1",p:"p",pre:"pre",...(0,r.R)(),...e.components};return(0,n.jsxs)(n.Fragment,{children:[(0,n.jsx)(s.h1,{id:"viewresponse",children:"ViewResponse"}),"\n",(0,n.jsxs)(s.p,{children:[(0,n.jsx)(s.code,{children:"ViewResponse"})," implements the ",(0,n.jsx)(s.a,{href:"https://www.http4k.org/api/org.http4k.template/-view-model/",children:"ViewModel"})," interface\nin http4k and allows passing the template name as a constructor argument, in order to avoid having\nto implement ",(0,n.jsx)(s.code,{children:"ViewModel.template()"})," every time."]}),"\n",(0,n.jsxs)(s.p,{children:["To lookup and render the template, the framework will use the settings and components previously bootstrapped\nwhen initialising Invirt via the ",(0,n.jsx)(s.code,{children:"Invirt()"})," filter wiring. Together with a few Invirt utility functions,\nthis allows the handler and view model code to be kept relatively simple."]}),"\n",(0,n.jsx)(s.pre,{children:(0,n.jsx)(s.code,{className:"language-kotlin",children:'data class ListUsersResponse(\n    val users: List<User>\n): ViewResponse("users/list") // Points to the template `users/list.peb`\n\nval handler =  routes(\n    "/users/list" GET {\n        ...\n        ListUsersResponse(users).ok()\n    },\n    "/users/create" POST {\n        ...\n        CreateUserResponse(user).status(Status.ACCEPTED)\n    }\n)\n'})})]})}function d(e={}){const{wrapper:s}={...(0,r.R)(),...e.components};return s?(0,n.jsx)(s,{...e,children:(0,n.jsx)(p,{...e})}):p(e)}},8453:(e,s,t)=>{t.d(s,{R:()=>i,x:()=>a});var n=t(6540);const r={},o=n.createContext(r);function i(e){const s=n.useContext(o);return n.useMemo((function(){return"function"==typeof e?e(s):{...s,...e}}),[s,e])}function a(e){let s;return s=e.disableParentContext?"function"==typeof e.components?e.components(r):e.components||r:i(e.components),n.createElement(o.Provider,{value:s},e.children)}}}]);