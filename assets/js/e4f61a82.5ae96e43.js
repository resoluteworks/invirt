"use strict";(self.webpackChunkdocs=self.webpackChunkdocs||[]).push([[6768],{3448:(e,n,t)=>{t.r(n),t.d(n,{assets:()=>d,contentTitle:()=>s,default:()=>v,frontMatter:()=>l,metadata:()=>c,toc:()=>u});var r=t(4848),i=t(8453),o=t(1470),a=t(9365);const l={sidebar_position:2},s="Environment",c={id:"api/invirt-core/environment",title:"Environment",description:"Environment.developmentMode",source:"@site/docs/api/invirt-core/environment.md",sourceDirName:"api/invirt-core",slug:"/api/invirt-core/environment",permalink:"/docs/api/invirt-core/environment",draft:!1,unlisted:!1,tags:[],version:"current",sidebarPosition:2,frontMatter:{sidebar_position:2},sidebar:"apiSidebar",previous:{title:"Route Binding",permalink:"/docs/api/invirt-core/route-binding"},next:{title:"Uri Extensions",permalink:"/docs/api/invirt-core/uri-extensions"}},d={},u=[{value:"Environment.developmentMode",id:"environmentdevelopmentmode",level:3},{value:"Environment.withDotEnv()",id:"environmentwithdotenv",level:3},{value:"Environment.withDotEnv(Dotenv)",id:"environmentwithdotenvdotenv",level:3},{value:"gitCommitId()",id:"gitcommitid",level:3}];function h(e){const n={a:"a",code:"code",h1:"h1",h3:"h3",p:"p",pre:"pre",...(0,i.R)(),...e.components};return(0,r.jsxs)(r.Fragment,{children:[(0,r.jsx)(n.h1,{id:"environment",children:"Environment"}),"\n",(0,r.jsx)(n.h3,{id:"environmentdevelopmentmode",children:"Environment.developmentMode"}),"\n",(0,r.jsxs)(n.p,{children:["Reads a variable ",(0,r.jsx)(n.code,{children:"DEVELOPMENT_MODE"})," from the receiver ",(0,r.jsx)(n.a,{href:"https://www.http4k.org/api/http4k-config/org.http4k.config/-environment/index.html",children:"Environment"})," which can be\nset on your local machine when running the application locally to enable hot reload capabilities\n(browser refresh loads template edits or static asset edits)."]}),"\n",(0,r.jsxs)(n.p,{children:[(0,r.jsx)(n.code,{children:"Environment.developmentMode"})," defaults to ",(0,r.jsx)(n.code,{children:"false"})," so in a production environment its absence implicitly\nenables classpath loading for views or static assets."]}),"\n",(0,r.jsxs)(o.A,{children:[(0,r.jsx)(a.A,{value:"example",label:"Example",default:!0,children:(0,r.jsx)(n.pre,{children:(0,r.jsx)(n.code,{className:"language-kotlin",children:'val devMode = Environment.ENV.developmentMode\ninitialiseInvirtViews(hotReload = devMode)\nval appHandler = InvirtFilter().then(\n    routes(\n        "/static/${assetsVersion}" bind staticAssets(devMode)\n    )\n)\n'})})}),(0,r.jsx)(a.A,{value:"declaration",label:"Declaration",children:(0,r.jsx)(n.pre,{children:(0,r.jsx)(n.code,{className:"language-kotlin",children:'val Environment.developmentMode: Boolean get() = EnvironmentKey.boolean().defaulted("DEVELOPMENT_MODE", false)(this)\n'})})})]}),"\n",(0,r.jsx)(n.h3,{id:"environmentwithdotenv",children:"Environment.withDotEnv()"}),"\n",(0,r.jsxs)(n.p,{children:["Loads environment variables from .env files and returns a new ",(0,r.jsx)(n.a,{href:"https://www.http4k.org/api/http4k-config/org.http4k.config/-environment/index.html",children:"Environment"}),"\nwith the combined variables from receiver environment and the ",(0,r.jsx)(n.code,{children:".env"})," file. The variables in the receiver ",(0,r.jsx)(n.code,{children:"Environment"}),"\noverride the ones in the .env files."]}),"\n",(0,r.jsxs)(n.p,{children:["An optional directory path argument (defaulting to ",(0,r.jsx)(n.code,{children:"./"}),")  can be passed to specify the location where to look up\nthe .env files."]}),"\n",(0,r.jsxs)(n.p,{children:["Invirt uses the ",(0,r.jsx)(n.a,{href:"https://github.com/cdimascio/dotenv-kotlin",children:"dotenv-kotlin"})," library underneath. Please see\nnext section for customising Dotenv loading."]}),"\n",(0,r.jsxs)(o.A,{children:[(0,r.jsx)(a.A,{value:"example",label:"Example",default:!0,children:(0,r.jsx)(n.pre,{children:(0,r.jsx)(n.code,{className:"language-kotlin",children:'// Environment containing system env vars combined with the ones in ./.env\nval env = Environment.ENV.withDotEnv()\n\n// Environment containing system env vars combined with the ones in /home/user/.env\nval env = Environment.ENV.withDotEnv("/home/user")\n'})})}),(0,r.jsx)(a.A,{value:"declaration",label:"Declaration",children:(0,r.jsx)(n.pre,{children:(0,r.jsx)(n.code,{className:"language-kotlin",children:'fun Environment.withDotEnv(dotEnvDirectory: String = "./"): Environment\n'})})})]}),"\n",(0,r.jsx)(n.h3,{id:"environmentwithdotenvdotenv",children:"Environment.withDotEnv(Dotenv)"}),"\n",(0,r.jsxs)(n.p,{children:["Allows overriding the settings the dotenv-kotlin uses to load .env files. You must add the ",(0,r.jsx)(n.a,{href:"https://github.com/cdimascio/dotenv-kotlin",children:"dotenv-kotlin"}),"\ndependency to your project to use this."]}),"\n",(0,r.jsxs)(o.A,{children:[(0,r.jsxs)(a.A,{value:"example",label:"Example",default:!0,children:[(0,r.jsx)(n.pre,{children:(0,r.jsx)(n.code,{className:"language-kotlin",children:'implementation("io.github.cdimascio:dotenv-kotlin:6.4.1")\n'})}),(0,r.jsx)(n.pre,{children:(0,r.jsx)(n.code,{className:"language-kotlin",children:'val dotEnv = dotenv {\n    directory = "../../"\n    ignoreIfMissing = false\n    systemProperties = true\n}\n\nval env = Environment.ENV.withDotEnv(dotEnv)\n'})})]}),(0,r.jsx)(a.A,{value:"declaration",label:"Declaration",children:(0,r.jsx)(n.pre,{children:(0,r.jsx)(n.code,{className:"language-kotlin",children:"fun Environment.withDotEnv(dotEnv: Dotenv): Environment\n"})})})]}),"\n",(0,r.jsx)(n.h3,{id:"gitcommitid",children:"gitCommitId()"}),"\n",(0,r.jsxs)(n.p,{children:["Returns the Git commit id, read from a property named ",(0,r.jsx)(n.code,{children:"git.commit.id"})," in a ",(0,r.jsx)(n.code,{children:"git.properties"})," file in the classpath.\nThe call fails if ",(0,r.jsx)(n.code,{children:"git.properties"})," cannot be found and returns ",(0,r.jsx)(n.code,{children:"null"})," if the file exists but doesn't contain\na ",(0,r.jsx)(n.code,{children:"git.commit.id"})," property."]}),"\n",(0,r.jsxs)(n.p,{children:["A ",(0,r.jsx)(n.code,{children:"git.propreties"})," can be created by your application's build process, or more commonly by using a Gradle plugin."]}),"\n",(0,r.jsx)(n.pre,{children:(0,r.jsx)(n.code,{className:"language-kotlin",children:'plugins {\n    id "com.gorylenko.gradle-git-properties" version "2.4.2"\n}\n'})}),"\n",(0,r.jsxs)(o.A,{children:[(0,r.jsx)(a.A,{value:"example",label:"Example",default:!0,children:(0,r.jsx)(n.pre,{children:(0,r.jsx)(n.code,{className:"language-kotlin",children:"val assetsVersion = gitCommitId()\n"})})}),(0,r.jsx)(a.A,{value:"declaration",label:"Declaration",children:(0,r.jsx)(n.pre,{children:(0,r.jsx)(n.code,{className:"language-kotlin",children:'fun gitCommitId(): String? = EnvironmentKey.optional("git.commit.id")(Environment.fromResource("git.properties"))\n'})})})]})]})}function v(e={}){const{wrapper:n}={...(0,i.R)(),...e.components};return n?(0,r.jsx)(n,{...e,children:(0,r.jsx)(h,{...e})}):h(e)}},9365:(e,n,t)=>{t.d(n,{A:()=>a});t(6540);var r=t(4164);const i={tabItem:"tabItem_Ymn6"};var o=t(4848);function a(e){let{children:n,hidden:t,className:a}=e;return(0,o.jsx)("div",{role:"tabpanel",className:(0,r.A)(i.tabItem,a),hidden:t,children:n})}},1470:(e,n,t)=>{t.d(n,{A:()=>w});var r=t(6540),i=t(4164),o=t(3104),a=t(6347),l=t(205),s=t(7485),c=t(1682),d=t(679);function u(e){return r.Children.toArray(e).filter((e=>"\n"!==e)).map((e=>{if(!e||(0,r.isValidElement)(e)&&function(e){const{props:n}=e;return!!n&&"object"==typeof n&&"value"in n}(e))return e;throw new Error(`Docusaurus error: Bad <Tabs> child <${"string"==typeof e.type?e.type:e.type.name}>: all children of the <Tabs> component should be <TabItem>, and every <TabItem> should have a unique "value" prop.`)}))?.filter(Boolean)??[]}function h(e){const{values:n,children:t}=e;return(0,r.useMemo)((()=>{const e=n??function(e){return u(e).map((e=>{let{props:{value:n,label:t,attributes:r,default:i}}=e;return{value:n,label:t,attributes:r,default:i}}))}(t);return function(e){const n=(0,c.X)(e,((e,n)=>e.value===n.value));if(n.length>0)throw new Error(`Docusaurus error: Duplicate values "${n.map((e=>e.value)).join(", ")}" found in <Tabs>. Every value needs to be unique.`)}(e),e}),[n,t])}function v(e){let{value:n,tabValues:t}=e;return t.some((e=>e.value===n))}function m(e){let{queryString:n=!1,groupId:t}=e;const i=(0,a.W6)(),o=function(e){let{queryString:n=!1,groupId:t}=e;if("string"==typeof n)return n;if(!1===n)return null;if(!0===n&&!t)throw new Error('Docusaurus error: The <Tabs> component groupId prop is required if queryString=true, because this value is used as the search param name. You can also provide an explicit value such as queryString="my-search-param".');return t??null}({queryString:n,groupId:t});return[(0,s.aZ)(o),(0,r.useCallback)((e=>{if(!o)return;const n=new URLSearchParams(i.location.search);n.set(o,e),i.replace({...i.location,search:n.toString()})}),[o,i])]}function p(e){const{defaultValue:n,queryString:t=!1,groupId:i}=e,o=h(e),[a,s]=(0,r.useState)((()=>function(e){let{defaultValue:n,tabValues:t}=e;if(0===t.length)throw new Error("Docusaurus error: the <Tabs> component requires at least one <TabItem> children component");if(n){if(!v({value:n,tabValues:t}))throw new Error(`Docusaurus error: The <Tabs> has a defaultValue "${n}" but none of its children has the corresponding value. Available values are: ${t.map((e=>e.value)).join(", ")}. If you intend to show no default tab, use defaultValue={null} instead.`);return n}const r=t.find((e=>e.default))??t[0];if(!r)throw new Error("Unexpected error: 0 tabValues");return r.value}({defaultValue:n,tabValues:o}))),[c,u]=m({queryString:t,groupId:i}),[p,f]=function(e){let{groupId:n}=e;const t=function(e){return e?`docusaurus.tab.${e}`:null}(n),[i,o]=(0,d.Dv)(t);return[i,(0,r.useCallback)((e=>{t&&o.set(e)}),[t,o])]}({groupId:i}),b=(()=>{const e=c??p;return v({value:e,tabValues:o})?e:null})();(0,l.A)((()=>{b&&s(b)}),[b]);return{selectedValue:a,selectValue:(0,r.useCallback)((e=>{if(!v({value:e,tabValues:o}))throw new Error(`Can't select invalid tab value=${e}`);s(e),u(e),f(e)}),[u,f,o]),tabValues:o}}var f=t(2303);const b={tabList:"tabList__CuJ",tabItem:"tabItem_LNqP"};var g=t(4848);function x(e){let{className:n,block:t,selectedValue:r,selectValue:a,tabValues:l}=e;const s=[],{blockElementScrollPositionUntilNextRender:c}=(0,o.a_)(),d=e=>{const n=e.currentTarget,t=s.indexOf(n),i=l[t].value;i!==r&&(c(n),a(i))},u=e=>{let n=null;switch(e.key){case"Enter":d(e);break;case"ArrowRight":{const t=s.indexOf(e.currentTarget)+1;n=s[t]??s[0];break}case"ArrowLeft":{const t=s.indexOf(e.currentTarget)-1;n=s[t]??s[s.length-1];break}}n?.focus()};return(0,g.jsx)("ul",{role:"tablist","aria-orientation":"horizontal",className:(0,i.A)("tabs",{"tabs--block":t},n),children:l.map((e=>{let{value:n,label:t,attributes:o}=e;return(0,g.jsx)("li",{role:"tab",tabIndex:r===n?0:-1,"aria-selected":r===n,ref:e=>s.push(e),onKeyDown:u,onClick:d,...o,className:(0,i.A)("tabs__item",b.tabItem,o?.className,{"tabs__item--active":r===n}),children:t??n},n)}))})}function j(e){let{lazy:n,children:t,selectedValue:i}=e;const o=(Array.isArray(t)?t:[t]).filter(Boolean);if(n){const e=o.find((e=>e.props.value===i));return e?(0,r.cloneElement)(e,{className:"margin-top--md"}):null}return(0,g.jsx)("div",{className:"margin-top--md",children:o.map(((e,n)=>(0,r.cloneElement)(e,{key:n,hidden:e.props.value!==i})))})}function E(e){const n=p(e);return(0,g.jsxs)("div",{className:(0,i.A)("tabs-container",b.tabList),children:[(0,g.jsx)(x,{...n,...e}),(0,g.jsx)(j,{...n,...e})]})}function w(e){const n=(0,f.A)();return(0,g.jsx)(E,{...e,children:u(e.children)},String(n))}},8453:(e,n,t)=>{t.d(n,{R:()=>a,x:()=>l});var r=t(6540);const i={},o=r.createContext(i);function a(e){const n=r.useContext(o);return r.useMemo((function(){return"function"==typeof e?e(n):{...n,...e}}),[n,e])}function l(e){let n;return n=e.disableParentContext?"function"==typeof e.components?e.components(i):e.components||i:a(e.components),r.createElement(o.Provider,{value:n},e.children)}}}]);