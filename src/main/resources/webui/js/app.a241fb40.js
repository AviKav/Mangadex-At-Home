(function(t){function e(e){for(var r,o,i=e[0],c=e[1],d=e[2],u=0,g=[];u<i.length;u++)o=i[u],Object.prototype.hasOwnProperty.call(s,o)&&s[o]&&g.push(s[o][0]),s[o]=0;for(r in c)Object.prototype.hasOwnProperty.call(c,r)&&(t[r]=c[r]);l&&l(e);while(g.length)g.shift()();return n.push.apply(n,d||[]),a()}function a(){for(var t,e=0;e<n.length;e++){for(var a=n[e],r=!0,i=1;i<a.length;i++){var c=a[i];0!==s[c]&&(r=!1)}r&&(n.splice(e--,1),t=o(o.s=a[0]))}return t}var r={},s={app:0},n=[];function o(e){if(r[e])return r[e].exports;var a=r[e]={i:e,l:!1,exports:{}};return t[e].call(a.exports,a,a.exports,o),a.l=!0,a.exports}o.m=t,o.c=r,o.d=function(t,e,a){o.o(t,e)||Object.defineProperty(t,e,{enumerable:!0,get:a})},o.r=function(t){"undefined"!==typeof Symbol&&Symbol.toStringTag&&Object.defineProperty(t,Symbol.toStringTag,{value:"Module"}),Object.defineProperty(t,"__esModule",{value:!0})},o.t=function(t,e){if(1&e&&(t=o(t)),8&e)return t;if(4&e&&"object"===typeof t&&t&&t.__esModule)return t;var a=Object.create(null);if(o.r(a),Object.defineProperty(a,"default",{enumerable:!0,value:t}),2&e&&"string"!=typeof t)for(var r in t)o.d(a,r,function(e){return t[e]}.bind(null,r));return a},o.n=function(t){var e=t&&t.__esModule?function(){return t["default"]}:function(){return t};return o.d(e,"a",e),e},o.o=function(t,e){return Object.prototype.hasOwnProperty.call(t,e)},o.p="/";var i=window["webpackJsonp"]=window["webpackJsonp"]||[],c=i.push.bind(i);i.push=e,i=i.slice();for(var d=0;d<i.length;d++)e(i[d]);var l=c;n.push([0,"chunk-vendors"]),a()})({0:function(t,e,a){t.exports=a("56d7")},"034f":function(t,e,a){"use strict";var r=a("8a23"),s=a.n(r);s.a},4678:function(t,e,a){var r={"./af":"2bfb","./af.js":"2bfb","./ar":"8e73","./ar-dz":"a356","./ar-dz.js":"a356","./ar-kw":"423e","./ar-kw.js":"423e","./ar-ly":"1cfd","./ar-ly.js":"1cfd","./ar-ma":"0a84","./ar-ma.js":"0a84","./ar-sa":"8230","./ar-sa.js":"8230","./ar-tn":"6d83","./ar-tn.js":"6d83","./ar.js":"8e73","./az":"485c","./az.js":"485c","./be":"1fc1","./be.js":"1fc1","./bg":"84aa","./bg.js":"84aa","./bm":"a7fa","./bm.js":"a7fa","./bn":"9043","./bn.js":"9043","./bo":"d26a","./bo.js":"d26a","./br":"6887","./br.js":"6887","./bs":"2554","./bs.js":"2554","./ca":"d716","./ca.js":"d716","./cs":"3c0d","./cs.js":"3c0d","./cv":"03ec","./cv.js":"03ec","./cy":"9797","./cy.js":"9797","./da":"0f14","./da.js":"0f14","./de":"b469","./de-at":"b3eb","./de-at.js":"b3eb","./de-ch":"bb71","./de-ch.js":"bb71","./de.js":"b469","./dv":"598a","./dv.js":"598a","./el":"8d47","./el.js":"8d47","./en-au":"0e6b","./en-au.js":"0e6b","./en-ca":"3886","./en-ca.js":"3886","./en-gb":"39a6","./en-gb.js":"39a6","./en-ie":"e1d3","./en-ie.js":"e1d3","./en-il":"7333","./en-il.js":"7333","./en-in":"ec2e","./en-in.js":"ec2e","./en-nz":"6f50","./en-nz.js":"6f50","./en-sg":"b7e9","./en-sg.js":"b7e9","./eo":"65db","./eo.js":"65db","./es":"898b","./es-do":"0a3c","./es-do.js":"0a3c","./es-us":"55c9","./es-us.js":"55c9","./es.js":"898b","./et":"ec18","./et.js":"ec18","./eu":"0ff2","./eu.js":"0ff2","./fa":"8df4","./fa.js":"8df4","./fi":"81e9","./fi.js":"81e9","./fil":"d69a","./fil.js":"d69a","./fo":"0721","./fo.js":"0721","./fr":"9f26","./fr-ca":"d9f8","./fr-ca.js":"d9f8","./fr-ch":"0e49","./fr-ch.js":"0e49","./fr.js":"9f26","./fy":"7118","./fy.js":"7118","./ga":"5120","./ga.js":"5120","./gd":"f6b4","./gd.js":"f6b4","./gl":"8840","./gl.js":"8840","./gom-deva":"aaf2","./gom-deva.js":"aaf2","./gom-latn":"0caa","./gom-latn.js":"0caa","./gu":"e0c5","./gu.js":"e0c5","./he":"c7aa","./he.js":"c7aa","./hi":"dc4d","./hi.js":"dc4d","./hr":"4ba9","./hr.js":"4ba9","./hu":"5b14","./hu.js":"5b14","./hy-am":"d6b6","./hy-am.js":"d6b6","./id":"5038","./id.js":"5038","./is":"0558","./is.js":"0558","./it":"6e98","./it-ch":"6f12","./it-ch.js":"6f12","./it.js":"6e98","./ja":"079e","./ja.js":"079e","./jv":"b540","./jv.js":"b540","./ka":"201b","./ka.js":"201b","./kk":"6d79","./kk.js":"6d79","./km":"e81d","./km.js":"e81d","./kn":"3e92","./kn.js":"3e92","./ko":"22f8","./ko.js":"22f8","./ku":"2421","./ku.js":"2421","./ky":"9609","./ky.js":"9609","./lb":"440c","./lb.js":"440c","./lo":"b29d","./lo.js":"b29d","./lt":"26f9","./lt.js":"26f9","./lv":"b97c","./lv.js":"b97c","./me":"293c","./me.js":"293c","./mi":"688b","./mi.js":"688b","./mk":"6909","./mk.js":"6909","./ml":"02fb","./ml.js":"02fb","./mn":"958b","./mn.js":"958b","./mr":"39bd","./mr.js":"39bd","./ms":"ebe4","./ms-my":"6403","./ms-my.js":"6403","./ms.js":"ebe4","./mt":"1b45","./mt.js":"1b45","./my":"8689","./my.js":"8689","./nb":"6ce3","./nb.js":"6ce3","./ne":"3a39","./ne.js":"3a39","./nl":"facd","./nl-be":"db29","./nl-be.js":"db29","./nl.js":"facd","./nn":"b84c","./nn.js":"b84c","./oc-lnc":"167b","./oc-lnc.js":"167b","./pa-in":"f3ff","./pa-in.js":"f3ff","./pl":"8d57","./pl.js":"8d57","./pt":"f260","./pt-br":"d2d4","./pt-br.js":"d2d4","./pt.js":"f260","./ro":"972c","./ro.js":"972c","./ru":"957c","./ru.js":"957c","./sd":"6784","./sd.js":"6784","./se":"ffff","./se.js":"ffff","./si":"eda5","./si.js":"eda5","./sk":"7be6","./sk.js":"7be6","./sl":"8155","./sl.js":"8155","./sq":"c8f3","./sq.js":"c8f3","./sr":"cf1e","./sr-cyrl":"13e9","./sr-cyrl.js":"13e9","./sr.js":"cf1e","./ss":"52bd","./ss.js":"52bd","./sv":"5fbd","./sv.js":"5fbd","./sw":"74dc","./sw.js":"74dc","./ta":"3de5","./ta.js":"3de5","./te":"5cbb","./te.js":"5cbb","./tet":"576c","./tet.js":"576c","./tg":"3b1b","./tg.js":"3b1b","./th":"10e8","./th.js":"10e8","./tl-ph":"0f38","./tl-ph.js":"0f38","./tlh":"cf75","./tlh.js":"cf75","./tr":"0e81","./tr.js":"0e81","./tzl":"cf51","./tzl.js":"cf51","./tzm":"c109","./tzm-latn":"b53d","./tzm-latn.js":"b53d","./tzm.js":"c109","./ug-cn":"6117","./ug-cn.js":"6117","./uk":"ada2","./uk.js":"ada2","./ur":"5294","./ur.js":"5294","./uz":"2e8c","./uz-latn":"010e","./uz-latn.js":"010e","./uz.js":"2e8c","./vi":"2921","./vi.js":"2921","./x-pseudo":"fd7e","./x-pseudo.js":"fd7e","./yo":"7f33","./yo.js":"7f33","./zh-cn":"5c3a","./zh-cn.js":"5c3a","./zh-hk":"49ab","./zh-hk.js":"49ab","./zh-mo":"3a6c","./zh-mo.js":"3a6c","./zh-tw":"90ea","./zh-tw.js":"90ea"};function s(t){var e=n(t);return a(e)}function n(t){if(!a.o(r,t)){var e=new Error("Cannot find module '"+t+"'");throw e.code="MODULE_NOT_FOUND",e}return r[t]}s.keys=function(){return Object.keys(r)},s.resolve=n,t.exports=s,s.id="4678"},"56d7":function(t,e,a){"use strict";a.r(e);a("e260"),a("e6cf"),a("cca6"),a("a79d");var r=a("2b0e"),s=function(){var t=this,e=t.$createElement,a=t._self._c||e;return a("v-app",{staticStyle:{background:"rgba(0,0,0,0)"}},[a("v-app-bar",{style:{background:t.$store.getters.current.primary+t.$store.getters.alpha},attrs:{app:"","clipped-left":t.$vuetify.breakpoint.mdAndUp,"hide-on-scroll":!t.$vuetify.breakpoint.mdAndUp,floating:""}},[a("v-app-bar-nav-icon",{staticClass:"hidden-md-and-up",style:{color:t.$store.getters.current.textColor},on:{click:function(e){e.stopPropagation(),t.drawer=!t.drawer}}}),a("v-img",{staticClass:"shrink mr-4",attrs:{alt:"MD Logo",contain:"",src:"https://mangadex.org/images/misc/navbar.svg?3",transition:"scale-transition",width:"60",hidden:!t.$vuetify.breakpoint.mdAndUp}}),a("h1",{staticClass:"hidden-sm-and-down",style:{color:t.$store.getters.current.textColor}},[t._v("MD@Home Client Interface")]),a("h2",{staticClass:"hidden-md-and-up",style:{color:t.$store.getters.current.textColor}},[t._v("MD@H Client")]),a("v-spacer")],1),a("v-navigation-drawer",{style:{background:t.$store.getters.current.primary+t.$store.getters.alpha},attrs:{"expand-on-hover":t.$vuetify.breakpoint.mdAndUp,"mini-variant":t.$vuetify.breakpoint.mdAndUp,permanent:t.$vuetify.breakpoint.mdAndUp,app:"",clipped:"",floating:""},model:{value:t.drawer,callback:function(e){t.drawer=e},expression:"drawer"}},[a("v-list",{staticClass:"pt-2",attrs:{dense:"",nav:"",dark:t.$store.getters.current.isDark}},[a("v-list-item",[a("v-list-item-icon",[a("v-icon",{style:{color:t.$store.getters.current.textColor}},[t._v("mdi-format-list-bulleted")])],1),a("v-list-item-content",[a("v-list-item-title",{style:{color:t.$store.getters.current.textColor}},[t._v("Menu ")])],1)],1),a("v-divider"),t._l(t.items,(function(e){return a("v-list-item",{key:e.title,staticClass:"mt-1",attrs:{to:{path:e.route},link:""}},[a("v-list-item-icon",[a("v-icon",{style:{color:t.$store.getters.current.textColor}},[t._v(t._s(e.icon))])],1),a("v-list-item-content",[a("v-list-item-title",{style:{color:t.$store.getters.current.textColor}},[t._v(t._s(e.title)+" ")])],1)],1)}))],2)],1),a("v-img",{style:{position:"fixed",top:0,left:0,width:"100%",height:"100%",display:t.$store.getters.hasBgImage?"initial":"none"},attrs:{src:t.$store.getters.bgImg}}),a("v-main",[a("transition",{attrs:{name:"fade"}},[a("router-view")],1)],1)],1)},n=[],o=(a("4160"),a("d3b7"),a("159b"),a("a434"),a("2f62"));r["a"].use(o["a"]);var i={current:"light",hasBgImage:!1,bgImage:"https://images3.alphacoders.com/819/thumb-1920-819294.png",themes:{light:{backgroundAlpha:"f0",backgroundColor:"#f0f0f0",textColor:"#202020",primary:"#fefefe",secondary:"#fdfdfd",accent:"#000000",accent1:"rgba(0,0,0,0.3)",accent2:"rgba(0,0,0,0.7)",green:"#00e000",red:"#e00000",yellow:"#e0e000",isDark:!1},dark:{backgroundAlpha:"f0",backgroundColor:"#353535",textColor:"#f0f0f0",primary:"#505050",secondary:"#454545",accent:"#606060",accent1:"rgba(200,200,200,0.3)",accent2:"rgba(200,200,200,0.7)",green:"#00e000",red:"#e00000",yellow:"#e0e000",isDark:!0},midnight:{backgroundAlpha:"f0",backgroundColor:"#111",textColor:"#dfdfdf",primary:"#202020",secondary:"#232323",accent:"#a0a0a0",accent1:"rgba(255,255,255,0.3)",accent2:"rgba(255,255,255,0.7)",green:"#00e000",red:"#e00000",yellow:"#e0e000",isDark:!0}},data:{stats:[],maxPoints:61,maxStorePoints:1801}},c={current:function(t){return t.themes[t.current]},hasBgImage:function(t){return t.hasBgImage},bgImg:function(t){return t.bgImage},alpha:function(t){return t.hasBgImage?t.themes[t.current].backgroundAlpha:"ff"},data:function(t){return t.data}},d={setTheme:function(t,e){t.current=e},setStats:function(t,e){t.data.stats=e},pushStats:function(t,e){t.data.stats.push(e),t.data.stats.length>t.data.maxStorePoints&&t.data.stats.splice(0,t.data.stats.length-t.data.maxStorePoints)},bg:function(t,e){t.hasBgImage=e},setBg:function(t,e){t.bgImage=e}},l=new o["a"].Store({state:i,mutations:d,getters:c}),u={name:"App",data:function(){return{drawer:null,settings:"settings",items:[{title:"Dashboard",icon:"mdi-view-dashboard",route:"/"},{title:"Settings",icon:"mdi-cog-outline",route:"/opts"}],evnt:Event}},mounted:function(){localStorage.stats&&l.getters.data.stats.length<1&&l.commit("setStats",JSON.parse(localStorage.stats)),fetch("/api/pastStats").then((function(t){return t.json()})).then((function(t){t.forEach((function(e){return l.commit("pushStats",JSON.parse("{"+e+": "+JSON.stringify(t[e])+"}"))}))})).catch((function(t){return console.log(t)})),setInterval((function(){fetch("/api/stats").then((function(t){return t.json()})).then((function(t){l.commit("pushStats",t),l.commit("changed",!0),localStorage.stats=JSON.stringify(l.getters.data.stats)})).catch((function(t){return console.log(t),!1}))}),2e3),Event.$emit("load")}},g=u,h=(a("034f"),a("2877")),f=a("6544"),p=a.n(f),b=a("7496"),m=a("40dc"),v=a("5bc1"),j=a("ce7e"),y=a("132d"),k=a("adda"),C=a("8860"),x=a("da13"),w=a("5d23"),_=a("34c3"),$=a("f6c4"),S=a("f774"),A=a("2fa4"),I=Object(h["a"])(g,s,n,!1,null,null,null),O=I.exports;p()(I,{VApp:b["a"],VAppBar:m["a"],VAppBarNavIcon:v["a"],VDivider:j["a"],VIcon:y["a"],VImg:k["a"],VList:C["a"],VListItem:x["a"],VListItemContent:w["a"],VListItemIcon:_["a"],VListItemTitle:w["b"],VMain:$["a"],VNavigationDrawer:S["a"],VSpacer:A["a"]});var T=a("f309");r["a"].use(T["a"]);var D=new T["a"]({}),V=function(){var t=this,e=t.$createElement,a=t._self._c||e;return a("v-container",{staticClass:"pa-5",style:{color:t.$store.getters.current.textColor},attrs:{fluid:""}},[a("v-container",{staticClass:"mb-3",style:{backgroundColor:t.$store.getters.current.secondary+t.$store.getters.alpha},attrs:{fluid:""}},[a("h1",[t._v("Dashboard")])]),a("v-row",[a("v-col",{attrs:{cols:"12",md:"4"}},[a("graph",{attrs:{"graph-id":"hitmiss",height:"300","md-up-height":"300",type:"doughnut",data:{labels:["Hits","Misses","Browser Cached"],datasets:t.hitmissdata},options:{maintainAspectRatio:!1,cutoutPercentage:65,centerText:{display:!0},legend:{labels:{fontColor:t.$store.getters.current.textColor}},title:{display:!0,text:"Reliability",fontColor:t.$store.getters.current.textColor}},plugins:[{beforeDraw:function(t,e){if(null!==t.config.options.centerText.display&&"undefined"!==typeof t.config.options.centerText.display&&t.config.options.centerText.display){var a=t.chart.width,r=t.chart.height,s=t.chart.ctx,n=(t.chartArea.left+t.chartArea.right)/2,o=(t.chartArea.top+t.chartArea.bottom)/2,i=(t.data.datasets[0].data[0]/(t.data.datasets[0].data[1]+t.data.datasets[0].data[0])*100).toFixed(2);"NaN"===i&&(i=0),s.restore(),s.textAlign="center";var c=(Math.min(r,a)/134).toFixed(2);s.font=c+"em sans-serif",s.textBaseline="middle",s.fillStyle=["hsl(",(i/100*120).toString(10),",100%,40%)"].join("");var d=i+"%";s.fillText(d,n,o),s.save()}}}]}})],1),a("v-col",{attrs:{cols:"12",md:"8"}},[a("graph",{attrs:{"graph-id":"netact",height:"250","md-up-height":"300",type:"line",data:{datasets:t.netactdata},options:{maintainAspectRatio:!1,elements:{line:{tension:0}},legend:{labels:{fontColor:t.$store.getters.current.textColor}},title:{display:!0,text:"Bytes Sent",fontColor:t.$store.getters.current.textColor},scales:t.dataScaleConfig}}})],1)],1),a("v-row",[a("v-col",{attrs:{cols:"12",md:"6"}},[a("graph",{attrs:{"graph-id":"reqserved",height:"250","md-up-height":"300",type:"line",data:{labels:[],datasets:t.reqdata},options:{maintainAspectRatio:!1,point:{pointBackgroundColor:"rgba(0,0,0,0)",pointBorderColor:"rgba(0,0,0,0)"},elements:{line:{tension:0}},legend:{labels:{fontColor:t.$store.getters.current.textColor}},title:{display:!0,text:"Requests Served",fontColor:t.$store.getters.current.textColor},scales:t.graphScaleConfig}}})],1),a("v-col",{attrs:{cols:"12",md:"6"}},[a("graph",{attrs:{"graph-id":"sizondisk",height:"250","md-up-height":"300",type:"line",data:{labels:[],datasets:t.diskdata},options:{maintainAspectRatio:!1,point:{pointBackgroundColor:"rgba(0,0,0,0)",pointBorderColor:"rgba(0,0,0,0)"},elements:{line:{tension:0}},legend:{labels:{fontColor:t.$store.getters.current.textColor}},title:{display:!0,text:"Space Used",fontColor:t.$store.getters.current.textColor},scales:t.dataScaleConfig}}})],1)],1),a("v-row"),a("v-btn",{attrs:{dark:t.$store.getters.current.isDark},on:{click:function(e){return t.pull()}}},[t._v("Update Now! ")])],1)},z=[],B=(a("99af"),a("b680"),a("b64b"),a("ac1f"),a("25f0"),a("5319"),function(){var t=this,e=t.$createElement,a=t._self._c||e;return a("v-container",{style:{backgroundColor:t.$store.getters.current.secondary+t.$store.getters.alpha,height:(t.$vuetify.breakpoint.mdAndUp?t.mdUpHeight:t.height)+"px"},attrs:{fluid:""}},[a("canvas",{attrs:{id:t.graphId}})])}),R=[],M=a("30ef"),q=a.n(M),P=[],E={name:"graph",props:["graphId","type","data","options","plugins","mdUpHeight","height"],methods:{createChart:function(t){var e=document.getElementById(t);P.push({chart:new q.a(e,{type:this.type,data:this.data,options:this.options,plugins:this.plugins}),chartid:t})},getChart:function(){return P},clearChart:function(){P=[]}},mounted:function(){this.createChart(this.graphId)}},U=E,L=a("a523"),N=Object(h["a"])(U,B,R,!1,null,"5ac2b79a",null),F=N.exports;p()(N,{VContainer:L["a"]});var H={name:"Dashboard",components:{Graph:F},data:function(){return{graphScaleConfig:{yAxes:[{ticks:{beginAtZero:!0,fontColor:l.getters.current.textColor,maxTicksLimit:11,padding:10},gridLines:{color:l.getters.current.accent,drawTicks:!1}}],xAxes:[{type:"time",ticks:{fontColor:l.getters.current.textColor,maxRotation:0,autoSkipPadding:22,padding:10},gridLines:{color:l.getters.current.accent,drawTicks:!1}}]},dataScaleConfig:{yAxes:[{ticks:{beginAtZero:!0,fontColor:l.getters.current.textColor,maxTicksLimit:11,padding:10,callback:function(t){var e=parseFloat(t.toString()),a=Math.abs(e);return a>=1e15?(e/1e15).toFixed(1)+"pb":a>=1e12?(e/1e12).toFixed(1)+"tb":a>=1e9?(e/1e9).toFixed(1)+"gb":a>=1e6?(e/1e6).toFixed(1)+"mb":a>=1e3?(e/1e3).toFixed(1)+"kb":e+"b"}},gridLines:{color:l.getters.current.accent,drawTicks:!1}}],xAxes:[{type:"time",ticks:{fontColor:l.getters.current.textColor,maxRotation:0,autoSkipPadding:22,padding:10},gridLines:{color:l.getters.current.accent,drawTicks:!1}}]},toolTips:{mode:"average"},hitmissdata:[{data:[0,0,0],backgroundColor:[l.getters.current.green,l.getters.current.red,l.getters.current.yellow],borderColor:l.getters.current.accent2}],netactdata:[{label:"Change",data:[],backgroundColor:l.getters.current.yellow+"d0",borderColor:l.getters.current.yellow+"d0",pointRadius:0,pointHitRadius:10,borderWidth:1},{label:"Total",data:[],backgroundColor:l.getters.current.accent2,borderColor:l.getters.current.accent+"d0",pointRadius:0,pointHitRadius:10,borderWidth:1}],diskdata:[{label:"Change",data:[],backgroundColor:l.getters.current.yellow,borderColor:l.getters.current.yellow+"d0",pointRadius:0,pointHitRadius:10,borderWidth:1},{label:"Total",data:[],backgroundColor:l.getters.current.accent2,borderColor:l.getters.current.accent+"d0",pointRadius:0,pointHitRadius:10,borderWidth:1}],reqdata:[{label:"Change",data:[],backgroundColor:l.getters.current.yellow,borderColor:l.getters.current.yellow+"d0",pointRadius:0,pointHitRadius:10,borderWidth:1},{label:"Total",data:[],backgroundColor:l.getters.current.accent2,borderColor:l.getters.current.accent+"d0",pointRadius:0,pointHitRadius:10,borderWidth:1}],evnt:Event}},methods:{updateValues:function(){var t=l.getters.data.stats;if(!(t.length<1)){var e=Object.keys(t[t.length-1])[0],a=t[t.length-1][e],r=t.length>2?t[t.length-2][Object.keys(t[t.length-2])[0]]:null,s=new Date(e.replace("T"," ").replace("Z"," ")),n=Date.UTC(s.getFullYear(),s.getMonth(),s.getDate(),s.getHours(),s.getMinutes(),s.getSeconds());this.netactdata[0].data.push({t:n,y:r?a.bytes_sent-r.bytes_sent:0}),this.netactdata[1].data.push({t:n,y:a.bytes_sent}),this.diskdata[0].data.push({t:n,y:r?a.bytes_on_disk-r.bytes_on_disk:0}),this.diskdata[1].data.push({t:n,y:a.bytes_on_disk}),this.reqdata[0].data.push({t:n,y:r?a.requests_served-r.requests_served:0}),this.reqdata[1].data.push({t:n,y:a.requests_served});var o=F.methods.getChart();for(var i in o[0].chart.data.datasets[0].data[0]=a.cache_hits,o[0].chart.data.datasets[0].data[1]=a.cache_misses,o[0].chart.data.datasets[0].data[2]=a.browser_cached,o)o[i].chart.data.datasets.forEach((function(t){t.data.length>l.getters.data.maxPoints&&t.data.splice(0,t.data.length-l.getters.data.maxPoints)})),o[i].chart.update()}},loadData:function(){var t=l.getters.data.stats,e=F.methods.getChart();if(!(t.length<1)){this.netactdata[0].data=[],this.netactdata[1].data=[],this.diskdata[0].data=[],this.diskdata[1].data=[],this.reqdata[0].data=[],this.reqdata[1].data=[];for(var a=Math.max(t.length-l.getters.data.maxPoints,0);a<t.length;a++){var r=Object.keys(t[a])[0],s=t[a][r],n=a>1?t[a-1][Object.keys(t[a-1])[0]]:null,o=new Date(r.replace("T"," ").replace("Z"," ")),i=Date.UTC(o.getFullYear(),o.getMonth(),o.getDate(),o.getHours(),o.getMinutes(),o.getSeconds());this.netactdata[0].data.push({t:i,y:n?s.bytes_sent-n.bytes_sent:0}),this.netactdata[1].data.push({t:i,y:s.bytes_sent}),this.diskdata[0].data.push({t:i,y:n?s.bytes_on_disk-n.bytes_on_disk:0}),this.diskdata[1].data.push({t:i,y:s.bytes_on_disk}),this.reqdata[0].data.push({t:i,y:n?s.requests_served-n.requests_served:0}),this.reqdata[1].data.push({t:i,y:s.requests_served}),e[0].chart.data.datasets[0].data[0]=s.cache_hits,e[0].chart.data.datasets[0].data[1]=s.cache_misses,e[0].chart.data.datasets[0].data[2]=s.browser_cached}for(var c in e)e[c].chart.update()}},sortValues:function(){var t=l.getters.data.stats,e=a(t);function a(t){if(t.length<=1)return t;for(var e=[],r=[],s=t.pop(),n=t.length,o=0;o<n;o++){var i=new Date(Object.keys(t[o])[0].replace("T"," ").replace("Z"," ")),c=new Date(Object.keys(t[o])[0].replace("T"," ").replace("Z"," "));i<=c?e.push(t[o]):r.push(t[o])}return[].concat(a(e),s,a(r))}l.commit("setStats",e)},pull:function(){fetch("/api/stats").then((function(t){return t.json()})).then((function(t){l.commit("pushStats",t),localStorage.stats=JSON.stringify(l.getters.data.stats)})).catch((function(t){console.log(t)}))}},beforeMount:function(){F.methods.clearChart(),this.sortValues(),this.loadData()},mounted:function(){var t=this;Event.$on("load",(function(){t.loadData()}))},computed:{stats:function(){return l.getters.data.stats}},watch:{stats:function(){this.updateValues()}}},J=H,W=a("8336"),Z=a("62ad"),Y=a("0fd9"),G=Object(h["a"])(J,V,z,!1,null,null,null),X=G.exports;p()(G,{VBtn:W["a"],VCol:Z["a"],VContainer:L["a"],VRow:Y["a"]});var K=function(){var t=this,e=t.$createElement,a=t._self._c||e;return a("v-container",{style:{color:t.$store.getters.current.textColor},attrs:{fluid:""}},[a("v-container",{style:{backgroundColor:t.$store.getters.current.secondary+t.$store.getters.alpha},attrs:{fluid:""}},[a("h1",[t._v("Console")])]),a("p",[t._v(" Stop looking for hidden pages ")]),a("v-img",{attrs:{src:"https://i.stack.imgur.com/jDfXy.jpg"}})],1)},Q=[],tt={name:"Console"},et=tt,at=Object(h["a"])(et,K,Q,!1,null,"9c546b6c",null),rt=at.exports;p()(at,{VContainer:L["a"],VImg:k["a"]});var st=function(){var t=this,e=t.$createElement,a=t._self._c||e;return a("v-container",{style:{color:t.$store.getters.current.textColor},attrs:{fluid:""}},[a("v-container",{staticClass:"mb-5",style:{backgroundColor:t.$store.getters.current.secondary+t.$store.getters.alpha},attrs:{fluid:""}},[a("h1",[t._v("Settings")])]),a("v-container",{style:{backgroundColor:t.$store.getters.current.secondary+t.$store.getters.alpha},attrs:{fluid:""}},[a("v-select",{attrs:{label:"Theme",items:t.items,"item-value":"val","item-text":"disp",value:t.$store.state.current,color:t.$store.getters.current.textColor,dark:t.$store.getters.current.isDark},on:{input:t.setTheme}}),a("v-switch",{attrs:{label:"Background Image",dark:t.$store.getters.current.isDark},on:{change:function(e){t.$store.commit("bg",t.bg),t.saveChanges()}},model:{value:t.bg,callback:function(e){t.bg=e},expression:"bg"}}),a("div",{attrs:{hidden:!t.bg}},[a("v-text-field",{attrs:{label:"Image URL",dark:t.$store.getters.current.isDark},on:{change:function(e){t.$store.commit("setBg",t.bgimg),t.saveChanges()}},model:{value:t.bgimg,callback:function(e){t.bgimg=e},expression:"bgimg"}}),a("h6",[t._v(";-;")])],1)],1)],1)},nt=[];localStorage.theme&&l.commit("setTheme",localStorage.theme),document.body.style.backgroundColor=l.getters.current.backgroundColor,localStorage.hasbg&&l.commit("bg",localStorage.hasbg),localStorage.bg&&l.commit("setBg",localStorage.bg);var ot={name:"Options",methods:{setTheme:function(t){l.state.current=t,localStorage.theme=t,l.commit("setTheme",t),document.body.style.backgroundColor=l.getters.current.backgroundColor},saveChanges:function(){localStorage.hasbg=l.getters.hasBgImage,localStorage.bg=l.getters.bgImg}},data:function(){return{items:[{disp:"Light",val:"light"},{disp:"Dark",val:"dark"},{disp:"Midnight",val:"midnight"}],bg:l.getters.hasBgImage,bgimg:l.getters.bgImg}}},it=ot,ct=a("b974"),dt=a("b73d"),lt=a("8654"),ut=Object(h["a"])(it,st,nt,!1,null,null,null),gt=ut.exports;p()(ut,{VContainer:L["a"],VSelect:ct["a"],VSwitch:dt["a"],VTextField:lt["a"]});var ht=function(){var t=this,e=t.$createElement,a=t._self._c||e;return a("v-container",{style:{color:t.$store.getters.current.textColor},attrs:{fluid:""}},[a("v-container",{style:{backgroundColor:t.$store.getters.current.secondary+t.$store.getters.alpha},attrs:{fluid:""}},[a("h1",[t._v("Info")])]),a("p",[t._v(" You shouldn't be here ")]),a("v-img",{attrs:{src:"https://i.kym-cdn.com/photos/images/newsfeed/001/273/780/f05.png"}})],1)},ft=[],pt={name:"About"},bt=pt,mt=Object(h["a"])(bt,ht,ft,!1,null,"544bb546",null),vt=mt.exports;p()(mt,{VContainer:L["a"],VImg:k["a"]});var jt=a("8c4f"),yt=[{path:"/",component:X},{path:"/cons",component:rt},{path:"/opts",component:gt},{path:"/info",component:vt}];r["a"].use(jt["a"]);var kt=new jt["a"]({routes:yt}),Ct=a("9483");Object(Ct["a"])("".concat("/","service-worker.js"),{ready:function(){console.log("App is being served from cache by a service worker.\nFor more details, visit https://goo.gl/AFskqB")},registered:function(){console.log("Service worker has been registered.")},cached:function(){console.log("Content has been cached for offline use.")},updatefound:function(){console.log("New content is downloading.")},updated:function(){console.log("New content is available; please refresh.")},offline:function(){console.log("No internet connection found. App is running in offline mode.")},error:function(t){console.error("Error during service worker registration:",t)}}),r["a"].config.productionTip=!1,window.Event=new r["a"],new r["a"]({render:function(t){return t(O)},router:kt,store:l,vuetify:D}).$mount("#app")},"8a23":function(t,e,a){}});
//# sourceMappingURL=app.a241fb40.js.map