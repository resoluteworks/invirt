"use strict";(self.webpackChunkdocs=self.webpackChunkdocs||[]).push([[9672],{6756:(e,n,t)=>{t.r(n),t.d(n,{assets:()=>c,contentTitle:()=>s,default:()=>p,frontMatter:()=>a,metadata:()=>l,toc:()=>m});var i=t(4848),o=t(8453);const r=t.p+"assets/images/form-basics-3f89726a9a57d4adeb48f967a2487b5d.png",a={sidebar_position:1},s="Form basics",l={id:"framework/forms/form-basics",title:"Form basics",description:"Example application",source:"@site/docs/framework/forms/form-basics.md",sourceDirName:"framework/forms",slug:"/framework/forms/form-basics",permalink:"/docs/framework/forms/form-basics",draft:!1,unlisted:!1,tags:[],version:"current",sidebarPosition:1,frontMatter:{sidebar_position:1},sidebar:"frameworkSidebar",previous:{title:"Filters",permalink:"/docs/framework/filters"},next:{title:"Form validation",permalink:"/docs/framework/forms/form-validation"}},c={},m=[];function d(e){const n={a:"a",code:"code",h1:"h1",p:"p",pre:"pre",...(0,o.R)(),...e.components};return(0,i.jsxs)(i.Fragment,{children:[(0,i.jsx)(n.h1,{id:"form-basics",children:"Form basics"}),"\n",(0,i.jsx)(n.p,{children:(0,i.jsx)(n.a,{href:"https://github.com/resoluteworks/invirt/tree/main/examples/form-basics",children:"Example application"})}),"\n",(0,i.jsxs)(n.p,{children:["Invirt provides a simple ",(0,i.jsx)(n.code,{children:"Request.toForm<FormType>()"})," utility to convert complex HTML form bodies to application model objects,\nwith support for arrays, maps and nested objects, similar to some other MVC frameworks."]}),"\n",(0,i.jsx)(n.p,{children:"Below is a (crude) example of a form with a variety of controls and inputs for collecting details\nabout an online order."}),"\n",(0,i.jsx)("img",{src:r,width:"600"}),"\n",(0,i.jsx)("br",{}),"\n",(0,i.jsx)("br",{}),"\n",(0,i.jsx)(n.p,{children:"A potential Kotlin data model for this form can be something along these lines."}),"\n",(0,i.jsx)(n.pre,{children:(0,i.jsx)(n.code,{className:"language-kotlin",children:'data class OrderForm(\n    val name: String,\n    val email: String,\n    val deliveryDetails: DeliveryDetails,\n    val notifications: Set<NotificationType>,\n    val quantities: Map<String, Int>\n)\n\ndata class DeliveryDetails(\n    val whenNotAtHome: String,\n    val deliveryDate: LocalDate\n)\n\nenum class NotificationType(val label: String) {\n    DISPATCHED("Dispatched"),\n    IN_TRANSIT("In transit"),\n    DELIVERED("Delivered")\n}\n'})}),"\n",(0,i.jsxs)(n.p,{children:["The HTML form inputs need to match the field names in our Kotlin data model, with nested fields using the dot notation\n",(0,i.jsx)(n.code,{children:"deliveryDetails.deliveryDate"}),", and maps or arrays using the square brackets notation ",(0,i.jsx)(n.code,{children:"quantities[Apples]"})," (no apostrophes\nor quotes required)."]}),"\n",(0,i.jsx)(n.pre,{children:(0,i.jsx)(n.code,{className:"language-html",children:'<form action="/save-order" method="POST">\n    <input type="text" name="name"/>\n    <input type="text" name="email"/>\n    <input type="date" name="deliveryDetails.deliveryDate"/>\n\n    <input type="radio" name="deliveryDetails.whenNotAtHome" value="Leave with neighbour"/>\n    <input type="radio" name="deliveryDetails.whenNotAtHome" value="Leave in the back"/>\n\n    <input type="checkbox" name="notifications" value="DISPATCHED"/>\n    <input type="checkbox" name="notifications" value="IN_TRANSIT"/>\n    <input type="checkbox" name="notifications" value="DELIVERED"/>\n\n    <input type="text" name="quantities[Apples]"/>\n    <input type="text" name="quantities[Oranges]"/>\n    ...\n</form>\n'})}),"\n",(0,i.jsxs)(n.p,{children:["Reading this form into the ",(0,i.jsx)(n.code,{children:"OrderForm"})," object in an http4k handler is then as simple as:"]}),"\n",(0,i.jsx)(n.pre,{children:(0,i.jsx)(n.code,{className:"language-kotlin",children:'"/save-order" POST { request ->\n    val form = request.toForm<OrderForm>()\n    // Process the form\n}\n'})}),"\n",(0,i.jsxs)(n.p,{children:["For the complete working example checkout the ",(0,i.jsx)(n.a,{href:"https://github.com/resoluteworks/invirt/tree/main/examples/form-basics",children:"example application"}),"."]})]})}function p(e={}){const{wrapper:n}={...(0,o.R)(),...e.components};return n?(0,i.jsx)(n,{...e,children:(0,i.jsx)(d,{...e})}):d(e)}},8453:(e,n,t)=>{t.d(n,{R:()=>a,x:()=>s});var i=t(6540);const o={},r=i.createContext(o);function a(e){const n=i.useContext(r);return i.useMemo((function(){return"function"==typeof e?e(n):{...n,...e}}),[n,e])}function s(e){let n;return n=e.disableParentContext?"function"==typeof e.components?e.components(o):e.components||o:a(e.components),i.createElement(r.Provider,{value:n},e.children)}}}]);