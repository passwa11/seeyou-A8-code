(function () {
	function B() {
		for (var a = scheduler.get_visible_events(), b = [], c = 0; c < this.y_unit.length; c++) {
			b[c] = [];
		}
		b[f] || (b[f] = []);
		for (c = 0; c < a.length; c++) {
			for (var f = this.order[a[c][this.y_property]], d = 0; this._trace_x[d + 1] && a[c].start_date >= this._trace_x[d + 1]; ) {
				d++;
			}
			for (; this._trace_x[d] && a[c].end_date > this._trace_x[d]; ) {
				b[f][d] || (b[f][d] = []), b[f][d].push(a[c]), d++;
			}
		}
		return b;
	}
	function v(a, b, c) {
		var f = 0, d = b ? a.end_date : a.start_date;
		if (d.valueOf() > scheduler._max_date.valueOf()) {
			d = scheduler._max_date;
		}
		var i = d - scheduler._min_date_timeline;
		if (i < 0) {
			k = 0;
		} else {
			var g = Math.round(i / (c * scheduler._cols[0]));
			if (g > scheduler._cols.length) {
				g = scheduler._cols.length;
			}
			for (var e = 0; e < g; e++) {
				f += scheduler._cols[e];
			}
			var j = scheduler.date.add(scheduler._min_date_timeline, scheduler.matrix[scheduler._mode].x_step * g, scheduler.matrix[scheduler._mode].x_unit), i = d - j, k = Math.floor(i / c);
		}
		f += b ? k - 14 : k + 1;
		return f;
	}
	var selectedDate = "";
	function C(a) {
		//吧已经勾选好的保持起来，等渲染完后恢复
		var _rooms = document.getElementsByName("autoSelectRoom");
		var _selecedRoom;
		for(var _i=0; _i<_rooms.length; _i++){
			if(_rooms[_i].checked){
				_selecedRoom = _rooms[_i];
				break;
			}
		}

		var b = "<table style='table-layout:fixed;' cellspacing='0' cellpadding='0'>", c = [];
		scheduler._load_mode && scheduler._load();
		if (this.render == "cell") {
			c = B.call(this);
		} else {
			for (var f = scheduler.get_visible_events(), d = 0; d < f.length; d++) {
				var i = this.order[f[d][this.y_property]];
				c[i] || (c[i] = []);
				c[i].push(f[d]);
			}
		}
		for (var g = 0, e = 0; e < scheduler._cols.length; e++) {
			g += scheduler._cols[e];
		}
		var j = new Date;
		this._step = j = (scheduler.date.add(j, this.x_step * this.x_size, this.x_unit) - j) / g;
		this._summ = g;
		var k = scheduler._colsS.heights = [];
		this._events_height = {};
		for (e = 0; e < this.y_unit.length; e++) {
			var evenOddClass;//lijl添加,奇偶行颜色不同
			if(e%2!=0){
				evenOddClass="#ffffff";
			}else{
				evenOddClass="";
			}

			var h = this._logic(this.render, this.y_unit[e], this);
			h.height = "height:60px;";//lijl设置固定行高
			scheduler._merge(h, {height:this.dy});
			if (this.section_autoheight && this.y_unit.length * h.height < a.offsetHeight) {
				h.height = Math.max(h.height, Math.floor((a.offsetHeight - 1) / this.y_unit.length));
			}
			scheduler._merge(h, {tr_className:"", style_height:"height:" + h.height + "px;", style_width:"width:" + (this.dx - 3) + "px;", td_className:"dhx_matrix_scell" + (scheduler.templates[this.name + "_scaley_class"](this.y_unit[e].key, this.y_unit[e].label, this) ? " " + scheduler.templates[this.name + "_scaley_class"](this.y_unit[e].key, this.y_unit[e].label, this) : ""), td_content:scheduler.templates[this.name + "_scale_label"](this.y_unit[e].key, this.y_unit[e].label, this.y_unit[e]), summ_width:"width:" + g + "px;", table_className:""});
			var o = "";
			if (c[e] && this.render != "cell") {
				c[e].sort(function (a, d) {
					return a.start_date > d.start_date ? 1 : -1;
				});
				for (var l = [], d = 0; d < c[e].length; d++) {
					for (var m = c[e][d], n = 0; l[n] && l[n].end_date > m.start_date; ) {
						n++;
					}
					l[n] = m;
					var ll=scheduler.render_timeline_event.call(this, m, n);
					console.log(ll,'lllll');
					o += ll ;
				}

			}
			if (this.fit_events) {
				var w = this._events_height[this.y_unit[e].key] || 0;
				h.height = w > h.height ? w : h.height;
				//h.style_height = "height:" + h.height + "px;";
				h.style_height = "height:61px;";//lijl设置固定行高
			}
			b += "<tr class='" + h.tr_className + "' style='" + h.style_height + ";background-color:"+evenOddClass+"'><td class='" + h.td_className + "' style='" + h.style_width + " height:" + (h.height - 1) + "px;'>" + h.td_content + "</td>";
			if (this.render == "cell") {
				for (d = 0; d < scheduler._cols.length; d++) {
					b += "<td class='dhx_matrix_cell " + scheduler.templates[this.name + "_cell_class"](c[e][d], this._trace_x[d], this.y_unit[e]) + "' style='width:" + (scheduler._cols[d] - 3) + "px'><div style='width:" + (scheduler._cols[d] - 3) + "px'>" + scheduler.templates[this.name + "_cell_value"](c[e][d]) + "</div></td>";
				}
			} else {
				b += "<td><div style='" + h.summ_width + " " + h.style_height + " position:relative;' class='dhx_matrix_line'>";
				b += o;
				b += "<table class='" + h.table_className + "' cellpadding='0' cellspacing='0' style='border-bottom:1px #DDD solid;" + h.summ_width + " " + h.style_height + "' >";
				var ddd = new Date();
				selectedDate = scheduler._min_date;
				var isNow = (selectedDate.getFullYear() == ddd.getFullYear()) && (selectedDate.getMonth()+1 == ddd.getMonth()+1) && (selectedDate.getDate() == ddd.getDate());
				var setDisable = ((ddd.getHours() - 8) * 4) + (ddd.getMinutes()/15);
				for (d = 0; d < scheduler._cols.length; d++) {
					if(d%4==0){//lijl添加,每隔四个小格就已实线显示//在style中加入border-left:1px #DDD solid;
						if(d<setDisable && isNow)
						  b += "<td class='dhx_matrix_cell " + scheduler.templates[this.name + "_cell_class"](c[e], this._trace_x[d], this.y_unit[e]) + "' style='border-left:1px #E5E5E5 solid; width:" + (scheduler._cols[d] - 3) + "px'><div style='width:" + (scheduler._cols[d] - 3) + "px;background: #e5e5e5'></div></td>";
						else
						  b += "<td class='dhx_matrix_cell " + scheduler.templates[this.name + "_cell_class"](c[e], this._trace_x[d], this.y_unit[e]) + "' style='border-left:1px #839DFE solid; width:" + (scheduler._cols[d] - 3) + "px'><div style='width:" + (scheduler._cols[d] - 3) + "px'></div></td>";
					}else{
					  if(d<setDisable && isNow)
					    b += "<td class='dhx_matrix_cell " + scheduler.templates[this.name + "_cell_class"](c[e], this._trace_x[d], this.y_unit[e]) + "' style='width:" + (scheduler._cols[d] - 3) + "px'><div style='width:" + (scheduler._cols[d] - 3) + "px;background: #e5e5e5'></div></td>";
					  else
					    b += "<td class='dhx_matrix_cell " + scheduler.templates[this.name + "_cell_class"](c[e], this._trace_x[d], this.y_unit[e]) + "' style='width:" + (scheduler._cols[d] - 3) + "px'><div style='width:" + (scheduler._cols[d] - 3) + "px'></div></td>";
					}
				}
				b += "</table>";
				b += "</div></td>";
			}
			b += "</tr>";
		}
		b += "</table>";
		this._matrix = c;
		a.innerHTML = b;
		scheduler._rendered = [];
		//恢复已经勾选的信息
		if(_selecedRoom && document.getElementById(_selecedRoom.getAttribute("id"))){
			document.getElementById(_selecedRoom.getAttribute("id")).checked = true;
		}
		for (var q = document.getElementsByTagName("DIV"), e = 0; e < q.length; e++) {
			q[e].getAttribute("event_id") && scheduler._rendered.push(q[e]);
		}
		for (e = 0; e < a.firstChild.rows.length; e++) {
			k.push(a.firstChild.rows[e].offsetHeight);
		}
	}
	function D(a) {
		var b = scheduler.xy.scale_height, c = this._header_resized || scheduler.xy.scale_height;
		scheduler._cols = [];
		scheduler._colsS = {height:0};
		this._trace_x = [];
		var f = scheduler._x - this.dx - 18, d = [this.dx], i = scheduler._els.dhx_cal_header[0];
		//i.style.width = d[0] + f + "px";
		for (var g = scheduler._min_date_timeline = scheduler._min_date, e = 0; e < this.x_size; e++) {
			this._trace_x[e] = new Date(g), g = scheduler.date.add(g, this.x_step, this.x_unit), scheduler._cols[e] = Math.floor(f / (this.x_size - e)), f -= scheduler._cols[e], d[e + 1] = d[e] + scheduler._cols[e];
		}
		a.innerHTML = "<div id='colTitle'></div>";//lijl去掉左上角的文字(将div设置为空)
		if (this.second_scale) {
			for (var j = this.second_scale.x_unit, k = [this._trace_x[0]], h = [], o = [this.dx, this.dx], l = 0, m = 0; m < this._trace_x.length; m++) {
				var n = this._trace_x[m], w = E(j, n, k[l]);
				w && (++l, k[l] = n, o[l + 1] = o[l]);
				var q = l + 1;
				h[l] = scheduler._cols[m] + (h[l] || 0);
				o[q] += scheduler._cols[m];
			}
			a.innerHTML = "<div></div><div></div>";
			var p = a.firstChild;
			p.style.height = c + "px";
			var v = a.lastChild;
			v.style.position = "relative";
			for (var r = 0; r < k.length; r++) {
				var t = k[r], u = scheduler.templates[this.name + "_second_scalex_class"](t), x = document.createElement("DIV");
				x.className = "dhx_scale_bar dhx_second_scale_bar" + (u ? " " + u : "");
				scheduler.set_xy(x, h[r] - 1, c - 3, o[r], 0);
				x.innerHTML = scheduler.templates[this.name + "_second_scale_date"](t);
				p.appendChild(x);
			}
		}
		scheduler.xy.scale_height = c;
		for (var a = a.lastChild, s = 0; s < this._trace_x.length; s++) {
			g = this._trace_x[s];
			scheduler._render_x_header(s, d[s], g, a);
			var y = scheduler.templates[this.name + "_scalex_class"](g);
			y && (a.lastChild.className += " " + y);
		}
		scheduler.xy.scale_height = b;
		var z = this._trace_x;
		a.onclick = function (a) {
			var d = A(a);
			d && scheduler.callEvent("onXScaleClick", [d.x, z[d.x], a || event]);
		};
		a.ondblclick = function (a) {
			var d = A(a);
			d && scheduler.callEvent("onXScaleDblClick", [d.x, z[d.x], a || event]);
		};
	}
	function E(a, b, c) {
		switch (a) {
		  case "day":
			return !(b.getDate() == c.getDate() && b.getMonth() == c.getMonth() && b.getFullYear() == c.getFullYear());
		  case "week":
			return !(scheduler.date.getISOWeek(b) == scheduler.date.getISOWeek(c) && b.getFullYear() == c.getFullYear());
		  case "month":
			return !(b.getMonth() == c.getMonth() && b.getFullYear() == c.getFullYear());
		  case "year":
			return b.getFullYear() != c.getFullYear();
		  default:
			return !1;
		}
	}
	function p(a) {
		if (a) {
			scheduler.set_sizes();
			t();
			var b = scheduler._min_date;
			D.call(this, scheduler._els.dhx_cal_header[0]);
			C.call(this, scheduler._els.dhx_cal_data[0]);
			scheduler._min_date = b;
			scheduler._els.dhx_cal_date[0].innerHTML = scheduler.templates[this.name + "_date"](scheduler._min_date, scheduler._max_date);
			scheduler._table_view = !0;
		}
	}
	function u() {
		if (scheduler._tooltip) {
			scheduler._tooltip.style.display = "none", scheduler._tooltip.date = "";
		}
	}
	function F(a, b, c) {
		if (a.render == "cell") {
			var f = b.x + "_" + b.y, d = a._matrix[b.y][b.x];
			if (!d) {
				return u();
			}
			d.sort(function (a, d) {
				return a.start_date > d.start_date ? 1 : -1;
			});
			if (scheduler._tooltip) {
				if (scheduler._tooltip.date == f) {
					return;
				}
				scheduler._tooltip.innerHTML = "";
			} else {
				var i = scheduler._tooltip = document.createElement("DIV");
				i.className = "dhx_tooltip";
				document.body.appendChild(i);
				i.onclick = scheduler._click.dhx_cal_data;
			}
			for (var g = "", e = 0; e < d.length; e++) {
				var j = d[e].color ? "background-color:" + d[e].color + ";" : "", k = d[e].textColor ? "color:" + d[e].textColor + ";" : "";
				g += "<div class='dhx_tooltip_line' event_id='" + d[e].id + "' style='" + j + "" + k + "'>";
				g += "<div class='dhx_tooltip_date'>" + (d[e]._timed ? scheduler.templates.event_date(d[e].start_date) : "") + "</div>";
				g += "<div class='dhx_event_icon icon_details'>&nbsp;</div>";
				g += scheduler.templates[a.name + "_tooltip"](d[e].start_date, d[e].end_date, d[e]) + "</div>";
			}
			scheduler._tooltip.style.display = "";
			scheduler._tooltip.style.top = "0px";
			scheduler._tooltip.style.left = document.body.offsetWidth - c.left - scheduler._tooltip.offsetWidth < 0 ? c.left - scheduler._tooltip.offsetWidth + "px" : c.left + b.src.offsetWidth + "px";
			scheduler._tooltip.date = f;
			scheduler._tooltip.innerHTML = g;
			scheduler._tooltip.style.top = document.body.offsetHeight - c.top - scheduler._tooltip.offsetHeight < 0 ? c.top - scheduler._tooltip.offsetHeight + b.src.offsetHeight + "px" : c.top + "px";
		}
	}
	function t() {
		// alert(1);
		dhtmlxEvent(scheduler._els.dhx_cal_data[0], "mouseover", function (a) {
			var b = scheduler.matrix[scheduler._mode];
			if (b) {
				var c = scheduler._locate_cell_timeline(a), a = a || event, f = a.target || a.srcElement;
				if (c) {
					return F(b, c, getOffset(c.src));
				}
			}
			u();
		});
		t = function () {
		};
	}
	function G(a) {
		for (var b = a.parentNode.childNodes, c = 0; c < b.length; c++) {
			if (b[c] == a) {
				return c;
			}
		}
		return -1;
	}
	function A(a) {
		for (var a = a || event, b = a.target ? a.target : a.srcElement; b && b.tagName != "DIV"; ) {
			b = b.parentNode;
		}
		if (b && b.tagName == "DIV") {
			var c = b.className.split(" ")[0];
			if (c == "dhx_scale_bar") {
				return {x:G(b), y:-1, src:b, scale:!0};
			}
		}
	}
	scheduler.matrix = {};
	scheduler._merge = function (a, b) {
		for (var c in b) {
			typeof a[c] == "undefined" && (a[c] = b[c]);
		}
	};
	scheduler.createTimelineView = function (a) {
		//设置表头高度为38
		scheduler.xy.scale_height = 38;
		scheduler._merge(a, {section_autoheight:!0, name:"matrix", x:"time", y:"time", x_step:1, x_unit:"hour", y_unit:"day", y_step:1, x_start:0, x_size:24, y_start:0, y_size:7, render:"cell", dx:200, dy:50, fit_events:!0, second_scale:!1, _logic:function (a, b, c) {
			var e = {};
			scheduler.checkEvent("onBeforeViewRender") && (e = scheduler.callEvent("onBeforeViewRender", [a, b, c]));
			return e;
		}});
		scheduler.checkEvent("onTimelineCreated") && scheduler.callEvent("onTimelineCreated", [a]);
		var b = scheduler.render_data;
		scheduler.render_data = function (d, c) {
			if (this._mode == a.name) {
				if (c) {
					for (var f = 0; f < d.length; f++) {
						this.clear_event(d[f]), this.render_timeline_event.call(this.matrix[this._mode], d[f], 0, !0);
					}
				} else {
					p.call(a, !0);
				}
			} else {
				return b.apply(this, arguments);
			}
		};
		scheduler.matrix[a.name] = a;
		scheduler.templates[a.name + "_cell_value"] = function (a) {
			return a ? a.length : "";
		};
		scheduler.templates[a.name + "_cell_class"] = function () {
			return "";
		};
		scheduler.templates[a.name + "_scalex_class"] = function () {
			return "";
		};
		scheduler.templates[a.name + "_second_scalex_class"] = function () {
			return "";
		};
		scheduler.templates[a.name + "_scaley_class"] = function () {
			return "";
		};
		scheduler.templates[a.name + "_scale_label"] = function (a, b) {
			return b;
		};
		scheduler.templates[a.name + "_tooltip"] = function (a, b, c) {
			return c.text;
		};
		scheduler.templates[a.name + "_date"] = function (a, b) {
			return a.getDay() == b.getDay() && b - a < 86400000 ? scheduler.templates.day_date(a) : scheduler.templates.week_date(a, b);
		};
		scheduler.templates[a.name + "_scale_date"] = scheduler.date.date_to_str(a.x_date || scheduler.config.hour_date);
		scheduler.templates[a.name + "_second_scale_date"] = scheduler.date.date_to_str(a.second_scale && a.second_scale.x_date ? a.second_scale.x_date : scheduler.config.hour_date);
		scheduler.date["add_" + a.name] = function (b, c) {
			return scheduler.date.add(b, (a.x_length || a.x_size) * c * a.x_step, a.x_unit);
		};
		scheduler.date[a.name + "_start"] = scheduler.date[a.x_unit + "_start"] || scheduler.date.day_start;
		scheduler.attachEvent("onSchedulerResize", function () {
			return this._mode == a.name ? (p.call(a, !0), !1) : !0;
		});
		scheduler.attachEvent("onOptionsLoad", function () {
			a.order = {};
			scheduler.callEvent("onOptionsLoadStart", []);
			for (var b = 0; b < a.y_unit.length; b++) {
				a.order[a.y_unit[b].key] = b;
			}
			scheduler.callEvent("onOptionsLoadFinal", []);
			scheduler._date && a.name == scheduler._mode && scheduler.setCurrentView(scheduler._date, scheduler._mode);
		});
		scheduler.callEvent("onOptionsLoad", [a]);
		scheduler[a.name + "_view"] = function () {
			scheduler.renderMatrix.apply(a, arguments);
		};
		if (a.render != "cell") {
			var c = new Date, f = scheduler.date.add(c, a.x_step, a.x_unit).valueOf() - c.valueOf();
			scheduler["mouse_" + a.name] = function (b) {
				var c = this._drag_event;
				if (this._drag_id) {
					c = this.getEvent(this._drag_id), this._drag_event._dhx_changed = !0;
				}
				b.x -= a.dx;
				for (var g = 0, e = 0, j = 0; e < this._cols.length - 1; e++) {
					if (g += this._cols[e], g > b.x) {
						break;
					}
				}
				for (g = 0; j < this._colsS.heights.length; j++) {
					if (g += this._colsS.heights[j], g > b.y) {
						break;
					}
				}
				b.fields = {};
				a.y_unit[j] || (j = a.y_unit.length - 1);
				b.fields[a.y_property] = c[a.y_property] = a.y_unit[j].key;
				b.x = 0;
				this._drag_mode == "new-size" && c.start_date * 1 == this._drag_start * 1 && e++;
				var k = e >= a._trace_x.length ? scheduler.date.add(a._trace_x[a._trace_x.length - 1], a.x_step, a.x_unit) : a._trace_x[e];
				b.y = Math.round((k - this._min_date) / (60000 * this.config.time_step));
				b.custom = !0;
				b.shift = f;
				return b;
			};
		}
	};
	scheduler.render_timeline_event = function (a, b, c) {
		var f = v(a, !1, this._step), d = v(a, !0, this._step), i = scheduler.xy.bar_height, g = 2 + b * i, e = i + g - 2, j = a[this.y_property];
		if (!this._events_height[j] || this._events_height[j] < e) {
			this._events_height[j] = e;
		}
		//lijl添加(杨栢元协助),判断是否有权限,如果有则显示删除按钮,没有则不显示
		var status=a.status;
		var state=a.state;
		var timeout=a.timeout;//是否超时(2为超时,1为正常)
		var k="";
		var stylebgcolor="dhx_cal_event_line";
		if(state==0){
			stylebgcolor="dhx_cal_event_line1";
		}else if(state==1){
			stylebgcolor="dhx_cal_event_line2";
		}
		var updateMtId=a.upmtid;//修改会议的Id
		if((updateMtId==a.meetingid) && (status==2 || status==null || ""==status || "undefined"==status)&&(timeout!=2 && timeout!=3)){//更改后的设置
			k=scheduler.templates.event_class(a.start_date, a.end_date, a), k = stylebgcolor +(k || ""), h = a.color ? "background-color:" + a.color + ";" : "", o = a.textColor ? "color:" + a.textColor + ";" : "", l = "<div event_id=\"" + a.id + "\" class=\"" + k + "\" style=\"" + h + "" + o + "position:absolute; top:" + g + "px; left:" + (f-2) + "px; width:" + (Math.max(0, d - f)+5) + "px;" + (a._text_style || "") + "\"><div style=\"margin:4px 30px 4px 8px\">" + scheduler.templates.event_bar_text(a.start_date, a.end_date, a) + "</div><div style=\"width:25px;position:absolute;right:0;top:0;margin:0;\"><a href=\"#\" onclick=\"javascript:showDelete();\" title=\""+"删除"+"\" class=\"remove_mtroom\">"+"删除"+"</a></div></div>";
		}else{//原来的设置
			// k=scheduler.templates.event_class(a.start_date, a.end_date, a), k = stylebgcolor +(k || ""), h = a.color ? "background-color:" + a.color + ";" : "", o = a.textColor ? "color:" + a.textColor + ";" : "", l = "<div event_id=\"" + a.id + "\" class=\"" + k + "\" style=\"" + h + "" + o + "position:absolute; top:" + g + "px; left:" + (f-2) + "px; width:" + (Math.max(0, d - f)+5) + "px;" + (a._text_style || "") + "\">" + scheduler.templates.event_bar_text(a.start_date, a.end_date, a) + "</div>";
			//zhou
			$.ajax({
				type: "POST",
				url: "/seeyon/ext/meetingInfoTip.do?method=index",
				data: {id: a.id + ''},
				dataType: "json",
				async: false,
				success: function (res) {
					var m = res.data;
					var html = "";
					html += "申请人：" + m.sqr + "；申请人电话：" + (typeof (m.sqrdh)!='undefined' && m.sqrdh!=0?m.sqrdh:'无') + "；申请部门：" + m.deptname + "；参会领导："+(typeof (m.ldname)!='undefined' && m.ldname !=null ?m.ldname:"无")+"；会议时间：" + m.time + "；用途：" + (typeof(m.description)!='undefined' && m.description!=0? m.description:'无')+"；会场要求："+(typeof (m.hcyq)!='undefined' && m.hcyq !=0?m.hcyq:"无");
					k=scheduler.templates.event_class(a.start_date, a.end_date, a), k = stylebgcolor +(k || ""), h = a.color ? "background-color:" + a.color + ";" : "", o = a.textColor ? "color:" + a.textColor + ";" : "", l = "<div event_id=\"" + a.id + "\" class=\"" + k + "\" style=\"" + h + "" + o + "position:absolute; top:" + g + "px; left:" + (f-2) + "px; width:" + (Math.max(0, d - f)+5) + "px;" + (a._text_style || "") + "\">" + scheduler.templates.event_bar_text(a.start_date, a.end_date, a) + "</div><div id=\"id_" + a.id + "\"  style=\"" + h + "" + o + "position:absolute; top:" + g + "px;height: 100%; left:" + (f-2) + "px; width:" + (Math.max(0, d - f)+5) + "px;\">"+html+"</div>";
				}
			});
		}
		if (c) {
			var m = document.createElement("DIV");
			m.innerHTML = l;
			var n = this.order[j], p = scheduler._els.dhx_cal_data[0].firstChild.rows[n].cells[1].firstChild;
			scheduler._rendered.push(m.firstChild);
			p.appendChild(m.firstChild);
		} else {
			return l;
		}
	};
	scheduler.renderMatrix = function (a) {
		scheduler._els.dhx_cal_data[0].scrollTop = 0;
		var b = scheduler.date[this.name + "_start"](scheduler._date);
		scheduler._min_date = scheduler.date.add(b, this.x_start * this.x_step, this.x_unit);
		scheduler._max_date = scheduler.date.add(scheduler._min_date, this.x_size * this.x_step, this.x_unit);
		scheduler._table_view = !0;
		if (this.second_scale) {
			if (a && !this._header_resized) {
				this._header_resized = scheduler.xy.scale_height, scheduler.xy.scale_height *= 2, scheduler._els.dhx_cal_header[0].className += " dhx_second_cal_header";
			}
			if (!a && this._header_resized) {
				scheduler.xy.scale_height /= 2;
				this._header_resized = !1;
				var c = scheduler._els.dhx_cal_header[0];
				c.className = c.className.replace(/ dhx_second_cal_header/gi, "");
			}
		}
		p.call(this, a);
	};
	scheduler._locate_cell_timeline = function (a) {
		for (var a = a || event, b = a.target ? a.target : a.srcElement; b && b.tagName != "TD"; ) {
			b = b.parentNode;
		}
		if (b && b.tagName == "TD") {
			var c = b.className.split(" ")[0];
			if (c == "dhx_matrix_cell") {
				if (scheduler._isRender("cell")) {
					return {x:b.cellIndex - 1, y:b.parentNode.rowIndex, src:b};
				} else {
					for (var f = b.parentNode; f && f.tagName != "TD"; ) {
						f = f.parentNode;
					}
					return {x:b.cellIndex, y:f.parentNode.rowIndex, src:b};
				}
			} else {
				if (c == "dhx_matrix_scell") {
					return {x:-1, y:b.parentNode.rowIndex, src:b, scale:!0};
				}
			}
		}
		return !1;
	};
	var H = scheduler._click.dhx_cal_data;
	scheduler._click.dhx_cal_data = function (a) {
		var b = H.apply(this, arguments), c = scheduler.matrix[scheduler._mode];
		if (c) {
			var f = scheduler._locate_cell_timeline(a);
			f && (f.scale ? scheduler.callEvent("onYScaleClick", [f.y, c.y_unit[f.y], a || event]) : scheduler.callEvent("onCellClick", [f.x, f.y, c._trace_x[f.x], (c._matrix[f.y] || {})[f.x] || [], a || event]));
		}
		return b;
	};
	scheduler.dblclick_dhx_matrix_cell = function (a) {
		var b = scheduler.matrix[scheduler._mode];
		if (b) {
			var c = scheduler._locate_cell_timeline(a);
			c && (c.scale ? scheduler.callEvent("onYScaleDblClick", [c.y, b.y_unit[c.y], a || event]) : scheduler.callEvent("onCellDblClick", [c.x, c.y, b._trace_x[c.x], (b._matrix[c.y] || {})[c.x] || [], a || event]));
		}
	};
	scheduler.dblclick_dhx_matrix_scell = function (a) {
		return scheduler.dblclick_dhx_matrix_cell(a);
	};
	scheduler._isRender = function (a) {
		return scheduler.matrix[scheduler._mode] && scheduler.matrix[scheduler._mode].render == a;
	};
	scheduler.attachEvent("onCellDblClick", function (a, b, c, f, d) {
		if (!(this.config.readonly || d.type == "dblclick" && !this.config.dblclick_create)) {
			var i = scheduler.matrix[scheduler._mode], g = {};
			g.start_date = i._trace_x[a];
			g.end_date = i._trace_x[a + 1] ? i._trace_x[a + 1] : scheduler.date.add(i._trace_x[a], i.x_step, i.x_unit);
			g[scheduler.matrix[scheduler._mode].y_property] = i.y_unit[b].key;
			scheduler.addEventNow(g, null, d);
		}
	});
	scheduler.attachEvent("onBeforeDrag", function () {
		return scheduler._isRender("cell") ? !1 : !0;
	});
})();

