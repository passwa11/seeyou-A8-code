(function (factory) {
    var nameSpace = 'field_5583959520879353687';
    if (!window[nameSpace]) {
        var Builder = factory();
        window[nameSpace] = {
            instance: {}
        };
        window[nameSpace].init = function (options) {
            window[nameSpace].instance[options.privateId] = new Builder(options);
        };
        window[nameSpace].isNotNull = function (obj) {
            return true;
        };
    }
})(function () {
    /**
     * 构造函数
     *
     * @param options
     * @constructor
     */
    function App(options) {
        var self = this;
        // 初始化参数
        self.initParams(options);
        // 初始化dom
        self.initDom();
        // 事件
        self.events();
    }

    var test = "test";

    App.prototype = {
        initParams: function (options) {
            var self = this;
            self.adaptation = options.adaptation;
            self.privateId = options.privateId;
            self.formMessage = options.formMessage;
            self.messageObj = options.getData;
            self.preUrl = options.url_prefix;
            for(var key in self.messageObj.formdata.formmains) {
                self.mainForm = self.messageObj.formdata.formmains[key];
            }
            //console.log("getData");
            //console.log(options.getData);
            //console.log(options);
            // 回填图片展示的控件ID
            //var imgField = FormBean.getFieldBeanByName("图片预览");
            self.backFieldName = "field0051";
        },
        initDom: function () {
            var self = this;
            //dynamicLoading.css(self.preUrl + 'css/zlc.css');
            self.appendChildDom();
        },
        events: function () {
            var self = this;
            // 监听是否数据刷新
            self.adaptation.ObserverEvent.listen('Event' + self.privateId, function () {
                self.messageObj = self.adaptation.childrenGetData(self.privateId);
                self.appendChildDom();
            });
        },
        appendChildDom: function () {
            var self = this;
            var domStructure = '<section class="customButton_box_content">' +
                '<div class="customButton_class_box ' + self.privateId + '" title="' + self.messageObj.display.escapeHTML() + '">' + self.messageObj.display.escapeHTML() + '</div>' +
                '</section>';

            $("#" + self.privateId).html(domStructure);
            $("." + self.privateId).off('click').on('click', function() {
                self.print(self.privateId, self.messageObj, self.adaptation);
            });
        },

        // 显示按钮
        print: function (privateId, messageObj, adaptation) {
            var self = this;
            var dialog = $.dialog({
                id: 'meetingSeat',
                url: this.preUrl + 'html/meetingSeat.html',
                width: 1200,
                height: 620,
                title: '会议排座',
                type: 'panel',
                transParams: {messageObj: messageObj},
                checkMax: true,
                closeParam: {
                    'show': false,
                    autoClose: false,
                    handler: function () {
                    }
                },
                buttons: [{
                    text: "保存",
                    handler: function () {
                        var returnData = dialog.getReturnValue();
                        if(!returnData.valid) {
                            if(returnData.interfaceError) {
                                dialog.close();
                            }
                            return;
                        }

                        // 每次渲染控件时删除当前已有的附件
                        var backFillCtrl = self.mainForm[self.backFieldName];
                        if(backFillCtrl.attachmentInfo && backFillCtrl.attachmentInfo.attachmentInfos && backFillCtrl.attachmentInfo.attachmentInfos.length !== 0){
                            var att = {
                                tableName : self.formMessage.tableName,
                                tableCategory : self.formMessage.tableCategory,
                                updateRecordId : "",        // 更新对应的数据行Id,主表控件没有，明细表控件有
                                handlerMode : 'delete',
                                fieldName : self.backFieldName,    // 回填控件对应的field_id
                                deleteAttchmentData  : [backFillCtrl.attachmentInfo.attachmentInfos[0].id]      // 数组格式的附件信息
                            };
                            
//                            var att1 = {
//                                tableName : self.formMessage.tableName,
//                                tableCategory : self.formMessage.tableCategory,
//                                updateRecordId : "",        // 更新对应的数据行Id,主表控件没有，明细表控件有
//                                handlerMode : 'delete',
//                                fieldName : self.backFieldName1,    // 回填控件对应的field_id
//                                deleteAttchmentData  : [backFillCtrl.attachmentInfo.attachmentInfos[0].id]      // 数组格式的附件信息
//                            };

                            adaptation.backfillFormAttachment(att, self.privateId);
                           // adaptation.backfillFormAttachment(att1, self.privateId);
                        }

                        // 需要的base64是html2canvas的promise协议回调生成，在自定义控件点击保存时必须得等待
                        // 此回调函数执行完毕再关闭弹框
                        var process = top.$.progressBar({
                            text: '数据保存中'
                        });
                        var interval = setInterval(function() {
                            // 验证canvas是否绘制完毕
                            if(dialog.getReturnValue().data.valid) {   
                            	var returnData = dialog.getReturnValue();
                                $.ajax({
                                    type: 'post',
                                    async: true,
                                    url: "/seeyon/rest/cap4/meetingSeat/saveMeetingSeatPeople",
                                    dataType: 'json',
                                    data: JSON.stringify(returnData),
                                    contentType: 'application/json',
                                    success: function (res) {
                                        res.data.attachment.createdate = res.data.createdate;
                                        res.data.attachment.fileUrl = res.data.fileUrl;
                                        res.data.attachment.id = res.data.id;
                                        var arr = [];
                                        arr.push(res.data.attachment);
                                        for(var i = 0; i < arr.length; i++) {
                                        	if(arr [i]) {
                                        		arr[i].size = arr[i].size.toString();
                                        	}
                                        }
                                        var attachment = {
                                            tableName : self.formMessage.tableName,
                                            tableCategory : self.formMessage.tableCategory,
                                            updateRecordId : "",               // 更新对应的数据行Id,主表控件没有，明细表控件有
                                            handlerMode : 'add',
                                            fieldName : self.backFieldName,    // 回填控件对应的field_id
                                            addAttchmentData : arr             // 数组格式的附件信息
                                        };
//                                        var attachment1 = {
//                                            tableName : self.formMessage.tableName,
//                                            tableCategory : self.formMessage.tableCategory,
//                                            updateRecordId : "",               // 更新对应的数据行Id,主表控件没有，明细表控件有
//                                            handlerMode : 'add',
//                                            fieldName : self.backFieldName1,    // 回填控件对应的field_id
//                                            addAttchmentData : arr             // 数组格式的附件信息
//                                        };

                                        adaptation.backfillFormAttachment(attachment, self.privateId);
//                                        adaptation.backfillFormAttachment(attachment1, self.privateId);
                                        process.close();
                                        dialog.close();
                                    }
                                });
                                
                                clearInterval(interval);
                            }
                        }, 500);
                    }
                }, {
                    text: "取消",
                    handler: function () {
                        dialog.close();
                    }
                }]
            });
        }
    };


    var dynamicLoading = {
        css: function (path) {
            if (!path || path.length === 0) {
                throw new Error('argument "path" is required !');
            }
            var head = document.getElementsByTagName('head')[0];
            var link = document.createElement('link');
            link.href = path;
            link.rel = 'stylesheet';
            link.type = 'text/css';
            head.appendChild(link);
        },
        js: function (path) {
            if (!path || path.length === 0) {
                throw new Error('argument "path" is required !');
            }
            var head = document.getElementsByTagName('head')[0];
            var script = document.createElement('script');
            script.src = path;
            script.type = 'text/javascript';
            head.appendChild(script);
        }
    };

    return App;
});