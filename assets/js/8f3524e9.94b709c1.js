"use strict";(self.webpackChunkdocs=self.webpackChunkdocs||[]).push([[8725],{888:(e,n,i)=>{i.r(n),i.d(n,{assets:()=>l,contentTitle:()=>a,default:()=>h,frontMatter:()=>s,metadata:()=>r,toc:()=>c});var t=i(4848),o=i(8453);const s={sidebar_position:0},a="Configuration",r={id:"framework/configuration",title:"Configuration",description:"As we've seen in the Quickstart example, a simple call to Invirt() wires",source:"@site/docs/framework/configuration.md",sourceDirName:"framework",slug:"/framework/configuration",permalink:"/docs/framework/configuration",draft:!1,unlisted:!1,tags:[],version:"current",sidebarPosition:0,frontMatter:{sidebar_position:0},sidebar:"frameworkSidebar",next:{title:"ViewResponse",permalink:"/docs/framework/views-response"}},l={},c=[{value:"Development mode",id:"development-mode",level:3},{value:"Pebble configuration",id:"pebble-configuration",level:3}];function d(e){const n={a:"a",code:"code",h1:"h1",h3:"h3",li:"li",p:"p",pre:"pre",ul:"ul",...(0,o.R)(),...e.components};return(0,t.jsxs)(t.Fragment,{children:[(0,t.jsx)(n.h1,{id:"configuration",children:"Configuration"}),"\n",(0,t.jsxs)(n.p,{children:["As we've seen in the ",(0,t.jsx)(n.a,{href:"/docs/overview/quickstart",children:"Quickstart"})," example, a simple call to ",(0,t.jsx)(n.code,{children:"Invirt()"})," wires\nthe framework with its default configuration. However, you can customise the behaviour of the framework\nby passing an ",(0,t.jsx)(n.code,{children:"InvirtConfig"})," object to this initialisation call."]}),"\n",(0,t.jsx)(n.p,{children:"The Invirt configuration object controls how the framework behaves in different environments, and allows you to customise\nthe Pebble template engine used by Invirt."}),"\n",(0,t.jsx)(n.p,{children:"Below is the configuration object with its default values. There are a set of sensible defaults which\nyou can override as needed."}),"\n",(0,t.jsx)(n.pre,{children:(0,t.jsx)(n.code,{className:"language-kotlin",children:'data class InvirtConfig(\n    val developmentMode: Boolean = Environment.ENV.developmentMode,\n    val pebble: InvirtPebbleConfig = InvirtPebbleConfig()\n)\n\ndata class InvirtPebbleConfig(\n    val classpathLocation: String = "webapp/views",\n    val hotReloadDirectory: String = "src/main/resources/webapp/views",\n    val extensions: List<Extension> = emptyList(),\n    val globalVariables: Map<String, Any> = emptyMap()\n)\n'})}),"\n",(0,t.jsx)(n.h3,{id:"development-mode",children:"Development mode"}),"\n",(0,t.jsxs)(n.p,{children:["The flag ",(0,t.jsx)(n.code,{children:"InvirtConfig.developmentMode"})," is used to enable hot reload capabilities when running the application locally.\nBy default, this flag is read from the ",(0,t.jsx)(n.code,{children:"DEVELOPMENT_MODE"})," environment variable as discussed\n",(0,t.jsx)(n.a,{href:"/docs/api/invirt-core/environment#environmentdevelopmentmode",children:"here"}),"."]}),"\n",(0,t.jsxs)(n.p,{children:["When set to ",(0,t.jsx)(n.code,{children:"true"}),", the framework will look for templates in the ",(0,t.jsx)(n.code,{children:"InvirtPebbleConfig.hotReloadDirectory"})," path,\nand any template edits will be immediately visible (for example via a browser refresh). This is typically useful\non a local development environment."]}),"\n",(0,t.jsxs)(n.p,{children:["When set to ",(0,t.jsx)(n.code,{children:"false"}),", the framework will look for templates in the ",(0,t.jsx)(n.code,{children:"InvirtPebbleConfig.classpathLocation"})," path\nwith additional caching capabilities using http4k's built-in components. This is typically used when deploying the application in production."]}),"\n",(0,t.jsx)(n.h3,{id:"pebble-configuration",children:"Pebble configuration"}),"\n",(0,t.jsxs)(n.p,{children:["The ",(0,t.jsx)(n.code,{children:"InvirtPebbleConfig"})," object allows you to customise the Pebble template engine used by Invirt."]}),"\n",(0,t.jsxs)(n.ul,{children:["\n",(0,t.jsxs)(n.li,{children:[(0,t.jsx)(n.code,{children:"classpathLocation"})," is the path where the framework will look for templates when ",(0,t.jsx)(n.code,{children:"developmentMode"})," is ",(0,t.jsx)(n.code,{children:"false"}),"."]}),"\n",(0,t.jsxs)(n.li,{children:[(0,t.jsx)(n.code,{children:"hotReloadDirectory"})," is the path where the framework will look for templates when ",(0,t.jsx)(n.code,{children:"developmentMode"})," is ",(0,t.jsx)(n.code,{children:"true"}),"."]}),"\n",(0,t.jsxs)(n.li,{children:[(0,t.jsx)(n.code,{children:"extensions"})," is a list of custom Pebble extensions that you can use to expose custom Pebble capabilities to your template rendering.\nSee this ",(0,t.jsx)(n.a,{href:"https://github.com/resoluteworks/invirt/blob/main/examples/security-authentication/src/main/kotlin/examples/authentication/Application.kt#L34",children:"example"}),"\nfor an application using a custom Pebble extension."]}),"\n",(0,t.jsxs)(n.li,{children:[(0,t.jsx)(n.code,{children:"globalVariables"})," is a map of global variables that will be available in all Pebble templates. Particularly useful for exposing\napplication-wide configuration or constants like a ",(0,t.jsx)(n.a,{href:"/docs/framework/static-assets",children:"static assets"})," version, for example."]}),"\n"]})]})}function h(e={}){const{wrapper:n}={...(0,o.R)(),...e.components};return n?(0,t.jsx)(n,{...e,children:(0,t.jsx)(d,{...e})}):d(e)}},8453:(e,n,i)=>{i.d(n,{R:()=>a,x:()=>r});var t=i(6540);const o={},s=t.createContext(o);function a(e){const n=t.useContext(s);return t.useMemo((function(){return"function"==typeof e?e(n):{...n,...e}}),[n,e])}function r(e){let n;return n=e.disableParentContext?"function"==typeof e.components?e.components(o):e.components||o:a(e.components),t.createElement(s.Provider,{value:n},e.children)}}}]);