"use strict";(self.webpackChunkdocs=self.webpackChunkdocs||[]).push([[1243],{5782:(e,t,n)=>{n.r(t),n.d(t,{assets:()=>s,contentTitle:()=>a,default:()=>d,frontMatter:()=>o,metadata:()=>c,toc:()=>l});var i=n(4848),r=n(8453);const o={sidebar_position:2},a="Core concepts",c={id:"framework/security/core-concepts",title:"Core concepts",description:"Principal",source:"@site/docs/framework/security/core-concepts.md",sourceDirName:"framework/security",slug:"/framework/security/core-concepts",permalink:"/docs/framework/security/core-concepts",draft:!1,unlisted:!1,tags:[],version:"current",sidebarPosition:2,frontMatter:{sidebar_position:2},sidebar:"frameworkSidebar",previous:{title:"Overview",permalink:"/docs/framework/security/overview"},next:{title:"Example application",permalink:"/docs/framework/security/example"}},s={},l=[{value:"Principal",id:"principal",level:2},{value:"Authenticator",id:"authenticator",level:2},{value:"AuthenticationFilter",id:"authenticationfilter",level:2}];function h(e){const t={code:"code",h1:"h1",h2:"h2",p:"p",pre:"pre",...(0,r.R)(),...e.components};return(0,i.jsxs)(i.Fragment,{children:[(0,i.jsx)(t.h1,{id:"core-concepts",children:"Core concepts"}),"\n",(0,i.jsx)(t.h2,{id:"principal",children:"Principal"}),"\n",(0,i.jsx)(t.p,{children:"Invirt's definition of Principal is similar to the one in other MVC and server frameworks, in the sense\nthat it refers to the currently authenticated entity or user operating the application."}),"\n",(0,i.jsx)(t.p,{children:"An essential difference, however, is that Invirt doesn't have a native concept of \"anonymous\"\nPrincipal (i.e. not authenticated user) as we felt this would add a layer of complexity that doesn't\nbenefit the framework's objectives. That being said, it shouldn't be hard for an application to handle this should it require to."}),"\n",(0,i.jsx)(t.p,{children:"In Invirt, the Principal object is defined as a marker interface with no properties or functions, and only\na companion object with some utilities for exposing the entity to the rest of the application."}),"\n",(0,i.jsx)(t.pre,{children:(0,i.jsx)(t.code,{className:"language-kotlin",children:'interface Principal {\n\n    companion object {\n        /**\n         * Returns the [Principal] on the current thread if present, `null` otherwise\n         */\n        val currentSafe: Principal? get() = principalThreadLocal.get()\n\n        /**\n         * Returns the [Principal] on the current thread if present, fails otherwise\n         */\n        val current: Principal get() = currentSafe ?: throw IllegalStateException("No Principal found on current threads")\n\n        /**\n         * Checks if a [Principal] is present on the current thread.\n         */\n        val isPresent: Boolean get() = principalThreadLocal.get() != null\n    }\n}\n'})}),"\n",(0,i.jsx)(t.p,{children:'An Invirt application will implement this interface in a "user" class to define its properties and behaviour\nand allow it to interact with the rest of the Invirt Security mini-framework. Again, we didn\'t\nwant to prescribe the attributes and features of a Principal, and we leave it to the application to do so.'}),"\n",(0,i.jsx)(t.h2,{id:"authenticator",children:"Authenticator"}),"\n",(0,i.jsx)(t.p,{children:"This component defines the logic to authenticate an HTTP request and is an interface with a single method\nthat the application must implement."}),"\n",(0,i.jsx)(t.pre,{children:(0,i.jsx)(t.code,{className:"language-kotlin",children:"interface Authenticator {\n    fun authenticate(request: Request): AuthenticationResponse\n}\n"})}),"\n",(0,i.jsxs)(t.p,{children:["This interface is typically wired in the ",(0,i.jsx)(t.code,{children:"AuthenticationFilter"})," discussed below, and its ",(0,i.jsx)(t.code,{children:"authenticate()"})," function\nmust respond with an ",(0,i.jsx)(t.code,{children:"AuthenticationResponse"})," indicating whether a Principal could be authenticated from the\nRequest's properties (typically cookies)."]}),"\n",(0,i.jsx)(t.pre,{children:(0,i.jsx)(t.code,{className:"language-kotlin",children:"sealed class AuthenticationResponse {\n\n    data object Unauthenticated : AuthenticationResponse()\n\n    data class Authenticated<P : Principal>(\n        val principal: P,\n        val newCookies: List<Cookie> = emptyList()\n    ) : AuthenticationResponse()\n}\n"})}),"\n",(0,i.jsx)(t.h2,{id:"authenticationfilter",children:"AuthenticationFilter"}),"\n",(0,i.jsxs)(t.p,{children:[(0,i.jsx)(t.code,{children:"AuthenticationFilter"})," is the main component responsible for calling ",(0,i.jsx)(t.code,{children:"Authenticator"})," above and storing\nthe principal (if authentication is successful) on the current Request (context), and on a ThreadLocal."]}),"\n",(0,i.jsxs)(t.p,{children:["When ",(0,i.jsx)(t.code,{children:"AuthenticationResponse.Authenticated"})," contains a non-empty ",(0,i.jsx)(t.code,{children:"newCookies"}),", ",(0,i.jsx)(t.code,{children:"AuthenticationFilter"}),"\nwill set these on the response, once the request completes. This is useful for both login scenarios, but also\nfor refreshing authentication credentials stored in cookies (like JWT tokens)."]}),"\n",(0,i.jsxs)(t.p,{children:["It's important to note that ",(0,i.jsx)(t.code,{children:"AuthenticationFilter"})," doesn't act as a gateway to prevent requests from proceeding\nwhen a principal isn't present, or if the Principal doesn't match certain criteria for accessing the resource.\nIt's within the remit of the application to handle that."]}),"\n",(0,i.jsxs)(t.p,{children:["The filter's responsibility is simply to extract a ",(0,i.jsx)(t.code,{children:"Principal"})," object from the request, via the ",(0,i.jsx)(t.code,{children:"Authenticator"}),"\ncomponent, set it on the current thread and request context, and clear these after the request completes."]})]})}function d(e={}){const{wrapper:t}={...(0,r.R)(),...e.components};return t?(0,i.jsx)(t,{...e,children:(0,i.jsx)(h,{...e})}):h(e)}},8453:(e,t,n)=>{n.d(t,{R:()=>a,x:()=>c});var i=n(6540);const r={},o=i.createContext(r);function a(e){const t=i.useContext(o);return i.useMemo((function(){return"function"==typeof e?e(t):{...t,...e}}),[t,e])}function c(e){let t;return t=e.disableParentContext?"function"==typeof e.components?e.components(r):e.components||r:a(e.components),i.createElement(o.Provider,{value:t},e.children)}}}]);