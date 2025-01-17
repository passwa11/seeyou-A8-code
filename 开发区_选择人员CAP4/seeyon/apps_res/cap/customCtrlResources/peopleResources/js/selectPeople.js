(function (factory) {
    var nameSpace = 'field_5461900594264253750';
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
            self.messageObj = options.getData;
            self.preUrl = options.url_prefix;

        },
        initDom: function () {
            var self = this;
            dynamicLoading.css(self.preUrl + 'css/zlc.css');
            dynamicLoading.js(self.preUrl + 'js/JsBarcode.all.min.js');
            // dynamicLoading.js(self.preUrl + 'js/layui-people.js');
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
            document.querySelector('#' + self.privateId).innerHTML = domStructure;
            document.querySelector('.' + self.privateId).addEventListener('click', function () {
                self.print(self.privateId, self.messageObj, self.adaptation);
            });
        }
        // 显示按钮
        , print: function (privateId, messageObj, adaptation) {

            var dialog = $.dialog({
                id: 'dialog',
                url: this.preUrl + '/html/selectpeople.html',
                width: 1250,
                height: 620,
                title: '人员选择',
                type: 'panel',
                transParams: {oldPlace: messageObj.value},
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
                        var peoples = dialog.getReturnValue();
                        //添加明细行并回填数据
                        var content = messageObj.formdata.content;
                        var arr = peoples.data;
                        var newPeople = new Array();
                        for (var i = 0; i < peoples.data.length; i++) {
                            var a = arr[i];
                            newPeople.push(a);
                        }
                        var jsonObj = {
                            "masterId": content.contentDataId + "",
                            "dataInfo": JSON.stringify(peoples),
                            "flag": "0"
                        };

                        $.ajax({
                            type: 'post',
                            async: false,
                            url: "/seeyon/rest/cap4/selectPeople/backfillpeopleInfo",
                            dataType: 'json',
                            data: JSON.stringify(jsonObj),
                            contentType: 'application/json',
                            success: function (res) {
                                // 判断是否需要添加
                                debugger;
                                if (res.code != 0) {
                                    $.alert(res.message);
                                    return;
                                }
                                var _list = res.data.data;
                                var rows = res.data.dataCount;
                                var currentId = res.data.recordId;

                                var tableName = res.data.tableName;
                                // var data = csdk.core.getFormData();
                                // var list = data.formsons.front_formson_1.records[0].recordId;
                                //添加一行空行到到明细表formson_001当前选中行后面
                                // var curRecordId = list ? list : null;
                                // var objArr=[];
                                // for (var k=0;k<_list.length;k++){
                                //     objArr.push({});
                                // }
                                var opts = {
                                    tableName: tableName,
                                    posRecordId: currentId,
                                    records: _list
                                };

                                csdk.core.addRecord(opts, function (err, newRecord) {
                                    if (err) {
                                        return;
                                    }
                                    if (null != currentId) {
                                        newRecord.unshift(currentId);
                                    }
                                    //添加成功，取得新记录的id
                                    for (let i = 0; i < newRecord.length-1; i++) {
                                        var addField = _list[i];
                                        for (var j = 0; j < addField.length; j++) {
                                            addField[j].recordId = newRecord[i];
                                        }
                                        csdk.core.setFieldData(addField);
                                    }
                                });
                                dialog.close();

                            }, error: function (res) {
                            }
                        });

                    }
                }, {
                    text: "取消",
                    handler: function () {
                        dialog.close()
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
    }

    return App;
});
