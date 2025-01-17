(function(){
	$(document).ready(function () {
		new MxtLayout({
			'id' : 'layout',
			'centerArea' : {
				'id' : 'center',
				'border' : false,
				'minHeight' :20
			},
			'northArea' : {
				'id' : 'north',
				'height' : 50,
				'sprit' : false,
				'border' : false
			},
		});
		var o = {};
		var searchobj = $.searchCondition({
	        top:23,
	        right:10,
	        //left:10,
	        //bottom:10,
	        //点搜索按钮取值
	        searchHandler: function(){
	        	var params = searchobj.g.getReturnValue();
	        	if(params != null){
	        		o={};
	        		if (params.condition == 'title'){
	        			o.title = params.value;
	        		}
	        		if(params.condition == 'importLevel'){
	        			o.importLevel=params.value;
	        		}
	        		if(params.condition == 'sender'){
	        			o.sender=params.value;
	        		}
	        		if(params.condition == 'createDate'){
	        			if(params.value[0]!=""){
	        				o.beginTime = params.value[0];
	        			}
	        			if(params.value[1]!=""){
	        				o.endTime = params.value[1];
	        			}
	        		}
	        		if(params.condition == 'dealDate'){
	        			if(params.value[0]!=""){
	        				o.dealBeginTime = params.value[0];
	        			}
	        			if(params.value[1]!=""){
	        				o.dealEndTime = params.value[1];
	        			}
	        		}
	        	}
	        	$('#banJieTable').ajaxgridLoad(o);
	        },
	        conditions: [{
	        	id: 'title',
	            name: 'title',
	            type: 'input',
	            text: "标题",//标题
	            value: 'title',
	            maxLength:100
	        }, {
	        	id: 'importent',
	            name: 'importent',
	            type: 'select',
	            text: "重要程度",//重要程度
	            value: 'importLevel',
	            //codecfg : "codeType:'java',codeId:'com.seeyon.apps.samples.test.enums.MyEnums'",
	            items: [{
	                text: '普通',
	                value: '1'
	            }, {
	                text: '平急（公文）/重要（协同、表单）',
	                value: '2'
	            }, {
	                text: '加急 (公文)/非常重要（协同、表单）',
	                value: '3'
	            }, {
	                text: '特急（公文）',
	                value: '4'
	            }, {
	                text: '特提（公文）',
	                value: '5'
	            }]
	        }, {
	        	id: 'sender',
	            name: 'sender',
	            type: 'input',
	            text: "发起人",//发起人
	            value: 'sender'
	        }, {
	        	id: 'datetime',
	            name: 'datetime',
	            type: 'datemulti',
	            text: "发起时间",//发起时间
	            value: 'createDate',
	            dateTime: false,
	            ifFormat:'%Y-%m-%d'
	        }, {
	        	id: 'dealtime',
	            name: 'dealtime',
	            type: 'datemulti',
	            text: "处理时间",//处理时间
	            value: 'dealDate',
	            dateTime: false,
	            ifFormat:'%Y-%m-%d'
	        }]
	    });
		//表格加载
		var grid = $('#banJieTable').ajaxgrid({
			colModel : [ {
				display : "id",
				name : 'id',
				sortable : true,
				width : '5%',
				type : 'checkbox'
			}, {
				display : "公文标题",
				name : 'subject',
				sortable : true,
				width : '12.5%'
			}, {
				display : "创建时间",
				name : 'create_time',
				sortable : true,
				width : '10%'
			}, {
				display : "处理时间",
				name : 'complete_time',
				sortable : true,
				width : '10%'
			}, {
				display : "发起者",
				name : 'create_person',
				sortable : true,
				width : '12.5%'
			}, {
				display : "公文文号",
				name : 'doc_mark',
				sortable : true,
				width : '12.5%'
			}, {
				display : "发文单位",
				name : 'send_unit',
				sortable : true,
				width : '12.5%'
			}, {
				display : "处理期限",
				name : 'deadline_datetime',
				sortable : true,
				width : '12.5%'
			}, {
				display : "分类",
				name : 'edoc_type',
				sortable : true,
				width : '10%'
			}],
			render : rend,
			click: openDetail,
			height : 200,
			showTableToggleBtn : true,
			parentId : 'center',
			vChange : true,
			vChangeParam : {
				overflow : "hidden",
				autoResize : true
			},
			isHaveIframe : true,
			slideToggleBtn : true,
			managerName : "xkjtManager",
			managerMethod : "findMoreXkjtLeaderBanJie"			
		});
		
		$("#banJieTable").ajaxgridLoad();
		
		function rend(txt,rowData, rowIndex, colIndex,colObj) {
			if (colObj.name == 'edoc_type'){
				if (txt == 0){
					return '<a onclick="javascript:window.top.vPortal.sectionHandler.multiRowVariableColumnTemplete.open_link(\'/edocController.do?method=entryManager&amp;entry=sendManager&amp;listType=listFinish\');" >发文</a>';
				}else{
					return '<a onclick="javascript:window.top.vPortal.sectionHandler.multiRowVariableColumnTemplete.open_link(\'/edocController.do?method=entryManager&amp;entry=sendManager&amp;listType=listFinish&objectId=-7217783385919962978\');" >收文</a>';
				}
			}
		      return txt;
		}

		function openDetail(data, r, c){
			var url = "edocController.do?method=edocDetailInDoc&summaryId="+data.id+"&openFrom=lenPotent&lenPotent=100";
			window.open(url,"_blank");
		}
		//$('#banJieTable').ajaxgridLoad(grid);

	});

})();






