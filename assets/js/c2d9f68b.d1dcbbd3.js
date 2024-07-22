"use strict";(self.webpackChunkdocs=self.webpackChunkdocs||[]).push([[1351],{2391:(e,r,n)=>{n.r(r),n.d(r,{assets:()=>c,contentTitle:()=>l,default:()=>p,frontMatter:()=>a,metadata:()=>d,toc:()=>h});var i=n(4848),s=n(8453);const t=n.p+"assets/images/form-validation-concept-94220c7dfdb5d58e46f3ae256b18cd60.png",o=n.p+"assets/images/form-validation-signup-screen-0d54b58033ea2be780ed6fa2ce10b0ba.png",a={sidebar_position:2},l="Form validation",d={id:"framework/forms/form-validation",title:"Form validation",description:"Design approach",source:"@site/docs/framework/forms/form-validation.md",sourceDirName:"framework/forms",slug:"/framework/forms/form-validation",permalink:"/docs/framework/forms/form-validation",draft:!1,unlisted:!1,tags:[],version:"current",sidebarPosition:2,frontMatter:{sidebar_position:2},sidebar:"frameworkSidebar",previous:{title:"Form basics",permalink:"/docs/framework/forms/form-basics"},next:{title:"Overview",permalink:"/docs/framework/data-querying/overview"}},c={},h=[{value:"Design approach",id:"design-approach",level:2},{value:"Explicit, server-side validation",id:"explicit-server-side-validation",level:4},{value:"Retaining input is (often) important",id:"retaining-input-is-often-important",level:4},{value:"Validation framework",id:"validation-framework",level:2},{value:"Submission flow",id:"submission-flow",level:2},{value:"Form and model",id:"form-and-model",level:3},{value:"Handling form validation",id:"handling-form-validation",level:3},{value:"Displaying error messages",id:"displaying-error-messages",level:3},{value:"Accessing errors from a Pebble macro",id:"accessing-errors-from-a-pebble-macro",level:3},{value:"A note on error messages",id:"a-note-on-error-messages",level:2}];function m(e){const r={a:"a",admonition:"admonition",code:"code",em:"em",h1:"h1",h2:"h2",h3:"h3",h4:"h4",li:"li",mdxAdmonitionTitle:"mdxAdmonitionTitle",p:"p",pre:"pre",ul:"ul",...(0,s.R)(),...e.components};return(0,i.jsxs)(i.Fragment,{children:[(0,i.jsx)(r.h1,{id:"form-validation",children:"Form validation"}),"\n",(0,i.jsx)(r.admonition,{type:"note",children:(0,i.jsx)(r.mdxAdmonitionTitle,{children:(0,i.jsx)(r.a,{href:"https://github.com/resoluteworks/invirt/tree/main/examples/form-validation",children:"Example application"})})}),"\n",(0,i.jsx)(r.h2,{id:"design-approach",children:"Design approach"}),"\n",(0,i.jsx)(r.p,{children:"Invirt's approach to form validation is based on general practices of how HTML forms\nshould work, and common UX principles. Below are some of the constraints\nand guiding principles for how Invirt implements these on top of http4k."}),"\n",(0,i.jsx)(r.h4,{id:"explicit-server-side-validation",children:"Explicit, server-side validation"}),"\n",(0,i.jsx)(r.p,{children:'Validation is an explicit step performed by the http4k handler, there\'s no "magic" and no annotations.\nBased on the validation result, the handler has to make an explicit decision about the outcome:\nrespond with the success state or re-render the form to allow the user to correct the input.'}),"\n",(0,i.jsx)(r.h4,{id:"retaining-input-is-often-important",children:"Retaining input is (often) important"}),"\n",(0,i.jsx)(r.p,{children:"It's common for an application to need to retain the incorrectly entered values after a validation failure.\nThis is essential in making sure the user doesn't have to re-key all the inputs."}),"\n",(0,i.jsxs)(r.p,{children:["When validation is performed server side, the approach is different to a single page application.\nThe handler needs to re-render the form on a validation error ",(0,i.jsx)(r.em,{children:"and"})," present the previously entered values.\nInvirt tries to make this process as frictionless as possible, but also gives the developer plenty of latitude\nfor customisation."]}),"\n",(0,i.jsx)(r.p,{children:"In summary, at a high level, this is what Invirt is going for."}),"\n",(0,i.jsx)("img",{src:t,width:"800"}),"\n",(0,i.jsx)(r.h2,{id:"validation-framework",children:"Validation framework"}),"\n",(0,i.jsxs)(r.p,{children:["Invirt uses the ",(0,i.jsx)(r.a,{href:"https://github.com/resoluteworks/validk",children:"Validk"})," Kotlin validation framework which your\napplication must wire as a dependency and use in its handlers to validate form inputs."]}),"\n",(0,i.jsx)(r.pre,{children:(0,i.jsx)(r.code,{className:"language-kotlin",children:'implementation "io.resoluteworks:validk:${validkVersion}"\n'})}),"\n",(0,i.jsx)(r.p,{children:"Validk is not part of the Invirt framework as it's been designed as a stand-alone re-usable\nlibrary. But it's under the tutelage of the same maintainers as Invirt."}),"\n",(0,i.jsx)(r.h2,{id:"submission-flow",children:"Submission flow"}),"\n",(0,i.jsx)(r.p,{children:"Below is a very basic example of a form collecting signup details for a user. The form\nvalidation has the following requirements:"}),"\n",(0,i.jsxs)(r.ul,{children:["\n",(0,i.jsx)(r.li,{children:"Name is required and must be at least 5 characters long."}),"\n",(0,i.jsx)(r.li,{children:"Email is required and must be a valid email address."}),"\n",(0,i.jsx)(r.li,{children:"Password is required and must be at least 8 characters long."}),"\n"]}),"\n",(0,i.jsx)(r.p,{children:'We want to display the relevant validation error messages for each field, but we also\nwant to present a "Please correct the errors below" message at the top, when the input doesn\'t\npass validation.'}),"\n",(0,i.jsx)("img",{src:o,width:"800"}),"\n",(0,i.jsx)("br",{}),"\n",(0,i.jsx)("br",{}),"\n",(0,i.jsx)(r.p,{children:"The right side of the image depicts the desired outcome of a form submission that returns\nvalidation errors. A key element here is that we want to return the previously entered value for\nName and Email, to avoid the user having to re-key these, but we don't want to send back a\npreviously entered invalid password."}),"\n",(0,i.jsx)(r.h3,{id:"form-and-model",children:"Form and model"}),"\n",(0,i.jsx)(r.p,{children:"Below are the (stripped down) HTML form and respective Kotlin object for handling the form above."}),"\n",(0,i.jsx)(r.pre,{children:(0,i.jsx)(r.code,{className:"language-html",children:'<form action="/signup" method="POST">\n    <input type="text" name="name"/>\n    <input type="text" name="email"/>\n    <input type="password" name="password"/>\n    <button type="submit">Sign up</button>\n</form>\n'})}),"\n",(0,i.jsx)(r.pre,{children:(0,i.jsx)(r.code,{className:"language-kotlin",children:"data class SignupForm(\n    val name: String,\n    val email: String,\n    val password: String,\n) : ValidObject<SignupForm> {\n\n    override val validation = Validation {\n        ...\n    }\n}\n"})}),"\n",(0,i.jsx)(r.h3,{id:"handling-form-validation",children:"Handling form validation"}),"\n",(0,i.jsx)(r.p,{children:"Below is the handler that would then read this form, validate it, and return a response based on this outcome,\nincluding error messages and previously entered input values."}),"\n",(0,i.jsx)(r.pre,{children:(0,i.jsx)(r.code,{className:"language-kotlin",children:'"/signup" POST { request ->\n    request.toForm<SignupForm>()\n        .validate {\n            error { form, errors ->\n                errorResponse(form, errors, "signup.peb")\n            }\n            success { form ->\n                // Signup user with the date on the form and redirect to /signup/success\n                httpSeeOther("/signup/success")\n            }\n        }\n}\n'})}),"\n",(0,i.jsxs)(r.p,{children:["But there are several steps here, so let's take it one at a time. Firstly, we read the form\ninto a ",(0,i.jsx)(r.code,{children:"SignupForm"})," object, with the construct discussed in the ",(0,i.jsx)(r.a,{href:"/docs/framework/forms/form-basics",children:"previous section"}),"."]}),"\n",(0,i.jsx)(r.pre,{children:(0,i.jsx)(r.code,{className:"language-kotlin",children:"request.toForm<SignupForm>()\n"})}),"\n",(0,i.jsxs)(r.p,{children:["Because ",(0,i.jsx)(r.code,{children:"SignupForm"})," implements Validk's ",(0,i.jsx)(r.code,{children:"ValidObject"})," interface it means we can call\n",(0,i.jsx)(r.code,{children:".validate"})," on this directly, which in turn allows us to provide custom handling\nlogic for success and failure scenarios."]}),"\n",(0,i.jsxs)(r.p,{children:["In both cases, we want to return an http4k ",(0,i.jsx)(r.code,{children:"Response"}),". For the success scenario\nwe simply return an HTTP 303 using Invirt's ",(0,i.jsx)(r.code,{children:"httpSeeOther"})," utility."]}),"\n",(0,i.jsxs)(r.p,{children:["For the error scenario, we want to return a view response, which renders the form again via\n",(0,i.jsx)(r.code,{children:"signup.peb"}),", the template we used to render the initial (empty) form."]}),"\n",(0,i.jsxs)(r.p,{children:["The error scenario uses Invirt's ",(0,i.jsx)(r.code,{children:"errorResponse"})," utility which produces an http4k view response\nwith a special implementation of the ",(0,i.jsx)(r.a,{href:"https://www.http4k.org/api/org.http4k.template/-view-model/",children:"ViewModel"}),"."]}),"\n",(0,i.jsx)(r.pre,{children:(0,i.jsx)(r.code,{className:"language-kotlin",children:"internal class ErrorResponseView(\n    val model: Any,\n    val errors: ValidationErrors,\n    val template: String\n) : ViewModel {\n    override fun template() = template\n}\n"})}),"\n",(0,i.jsxs)(r.p,{children:["When returning ",(0,i.jsx)(r.code,{children:'errorResponse(form, errors, "signup.peb")'}),", Invirt's custom Pebble rendering detects\nthat we're trying to render an error response and exposes the passed ",(0,i.jsx)(r.code,{children:"errors"})," argument into the\ntemplate context, and the form as the ",(0,i.jsx)(r.code,{children:"model"}),". You can read more about accessing errors in your template\n",(0,i.jsx)(r.a,{href:"/docs/api/invirt-core/pebble/pebble-context-objects#errors",children:"here"}),"."]}),"\n",(0,i.jsx)(r.h3,{id:"displaying-error-messages",children:"Displaying error messages"}),"\n",(0,i.jsx)(r.p,{children:"There are then two things we can add to our HTML form. First, we can set a value for our inputs\nto display a previously entered value. Second, we can show an error message for each input\nby checking if the field has any errors. This is a common pattern that you've likely encountered\nin other MVC frameworks."}),"\n",(0,i.jsxs)(r.p,{children:["To omit the password value from being rendered back on the form, we simply don't\nadd a ",(0,i.jsx)(r.code,{children:"value"})," to the password input, essentially leaving it as per earlier definition."]}),"\n",(0,i.jsx)(r.pre,{children:(0,i.jsx)(r.code,{className:"language-html",children:'<input type="text" name="name" value="{{ model.name }}"/>\n\n{% if errors.hasErrors("name") %}\n    <div class="text-error">{{ errors.error("name") }}</div>\n{% endif %}\n\n...\n\n<input type="password" name="password"/>\n\n{% if errors.hasErrors("password") %}\n    <div class="text-error">{{ errors.error("password") }}</div>\n{% endif %}\n'})}),"\n",(0,i.jsx)(r.p,{children:"Lastly, we can also check for the presence of validation errors and display the custom message\nat the top of the form."}),"\n",(0,i.jsx)(r.pre,{children:(0,i.jsx)(r.code,{className:"language-html",children:'{% if errors != null %}\n    <div class="text-lg font-semibold text-error">Please correct the errors below</div>\n{% endif %}\n'})}),"\n",(0,i.jsx)(r.h3,{id:"accessing-errors-from-a-pebble-macro",children:"Accessing errors from a Pebble macro"}),"\n",(0,i.jsxs)(r.p,{children:[(0,i.jsx)(r.a,{href:"https://pebbletemplates.io/wiki/tag/macro/",children:"Pebble macros"})," don't have the context of the view being rendered,\nso the ",(0,i.jsx)(r.code,{children:"errors"})," object above (or ",(0,i.jsx)(r.code,{children:"model"}),", for that matter) won't be accessible from a macro. When you need to handle\nvalidation errors within a macro, simply use the ",(0,i.jsx)(r.code,{children:"error()"})," function instead."]}),"\n",(0,i.jsx)(r.pre,{children:(0,i.jsx)(r.code,{className:"language-html",children:'{% if errors() != null %}\n    <div class="text-lg font-semibold text-error">Please correct the errors below</div>\n{% endif %}\n...\n{% if errors().hasErrors("password") %}\n    <div>{{ errors().error("password") }}</div>\n{% endif %}\n'})}),"\n",(0,i.jsx)(r.h2,{id:"a-note-on-error-messages",children:"A note on error messages"}),"\n",(0,i.jsxs)(r.p,{children:["By default, the Validk framework stops after the first validation error for a field, and returns\nonly that error message for it (fail-fast). This can be turned off\nso it returns the complete list of failures for a field, when that's required.\nYou can read more about this ",(0,i.jsx)(r.a,{href:"https://github.com/resoluteworks/validk?tab=readme-ov-file#fail-fast-validation",children:"here"}),"."]})]})}function p(e={}){const{wrapper:r}={...(0,s.R)(),...e.components};return r?(0,i.jsx)(r,{...e,children:(0,i.jsx)(m,{...e})}):m(e)}},8453:(e,r,n)=>{n.d(r,{R:()=>o,x:()=>a});var i=n(6540);const s={},t=i.createContext(s);function o(e){const r=i.useContext(t);return i.useMemo((function(){return"function"==typeof e?e(r):{...r,...e}}),[r,e])}function a(e){let r;return r=e.disableParentContext?"function"==typeof e.components?e.components(s):e.components||s:o(e.components),i.createElement(t.Provider,{value:r},e.children)}}}]);