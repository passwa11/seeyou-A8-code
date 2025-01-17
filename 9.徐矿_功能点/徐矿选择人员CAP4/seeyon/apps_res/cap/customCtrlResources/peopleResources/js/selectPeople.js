(function (factory) {
    var nameSpace = 'field_6530795810723998937';
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
                        var num = peoples.data.length + 1;
                        //2019-06-11
                        var arr = peoples.data;
                        var conditionId = new Array();
                        var newPeople = new Array();
                        for (var i = 0; i < peoples.data.length; i++) {
                            var a = arr[i];
                            var flag = a.flag;
                            if (flag == 'dzb') {
                                var id = a.id;
                                conditionId.push(id);
                            } else {
                                newPeople.push(a);
                            }
                        }
                        if (conditionId.length == 0) {
                            var jsonObj = {
                                "masterId": content.contentDataId + "",
                                "dataInfo": JSON.stringify(peoples),
                                "flag": "0"
                            };

                            $.ajax({
                                type: 'post',
                                async: true,
                                url: "/seeyon/rest/cap4/selectPeople/backfillpeopleInfo2",
                                dataType: 'json',
                                data: JSON.stringify(jsonObj),
                                contentType: 'application/json',
                                success: function (res) {
                                    // 判断是否需要添加
                                    if (res.code != 0) {
                                        $.alert(res.message);
                                        return;
                                    }

                                    var dataCount = res.data.dataCount + 1;
                                    addLineAndFilldata(content, adaptation, messageObj, privateId, peoples, dataCount);

                                }, error: function (res) {
                                }
                            });

                            function addLineAndFilldata(content, adaptation, messageObj, privateId, datainfo, flag) {
                                flag--;
                                var jsonAdd = {
                                    'masterId': content.contentDataId,
                                    'dataInfo': JSON.stringify(datainfo),
                                    'flag': flag + ""
                                };
                                $.ajax({
                                    type: 'post',
                                    async: true,
                                    url: "/seeyon/rest/cap4/selectPeople/backfillpeopleInfo2",
                                    dataType: 'json',
                                    data: JSON.stringify(jsonAdd),
                                    contentType: 'application/json',
                                    success: function (res) {
                                        // 判断是否需要添加
                                        if (res.code != 0) {
                                            $.alert(res.message);
                                            return;
                                        }
                                        var isNext = res.data.add;
                                        var val = 0;

                                        if (isNext) {
                                            var addLineParam = {};
                                            var dataCount = res.data.dataCount;

                                            addLineParam.tableName = res.data.tableName;
                                            addLineParam.isFormRecords = true;
                                            addLineParam.callbackFn = function () {
                                                addLineAndFilldata(content, adaptation, messageObj, privateId, peoples, flag);
                                            }
                                            window.thirdPartyFormAPI.insertFormsonRecords(addLineParam);
                                        } else {
                                            var dataList = res.data.data;
                                            if (null != dataList && dataList != '') {
                                                for (var i = 0; i < dataList.length; i++) {
                                                    var backfill = {};
                                                    backfill.tableName = res.data.tableName;
                                                    backfill.tableCategory = "formson";
                                                    backfill.updateData = dataList[i][dataList[i].recordId];
                                                    backfill.updateRecordId = dataList[i].recordId;
                                                    adaptation.backfillFormControlData(backfill, privateId);

                                                }
                                            }

                                        }

                                    }
                                });
                            }

                            dialog.close();
                        } else {
                            $.ajax({
                                type: 'post',
                                async: true,
                                url: '/seeyon/ext/selectPeople.do?method=selectPeopleByDeskWorkId&id=' + conditionId,
                                dataType: 'json',
                                contentType: 'application/json',
                                success: function (res) {
                                    var c = newPeople.concat(res.data);
                                    peoples['data'] = c;
                                    var jsonObj = {
                                        "masterId": content.contentDataId + "",
                                        "dataInfo": JSON.stringify(peoples),
                                        "flag": "0"
                                    };

                                    $.ajax({
                                        type: 'post',
                                        async: true,
                                        url: "/seeyon/rest/cap4/selectPeople/backfillpeopleInfo2",
                                        dataType: 'json',
                                        data: JSON.stringify(jsonObj),
                                        contentType: 'application/json',
                                        success: function (res) {
                                            // 判断是否需要添加
                                            if (res.code != 0) {
                                                $.alert(res.message);
                                                return;
                                            }

                                            var dataCount = res.data.dataCount + 1;
                                            addLineAndFilldata(content, adaptation, messageObj, privateId, peoples, dataCount);

                                        }, error: function (res) {
                                        }
                                    });

                                    function addLineAndFilldata(content, adaptation, messageObj, privateId, datainfo, flag) {
                                        flag--;
                                        var jsonAdd = {
                                            'masterId': content.contentDataId,
                                            'dataInfo': JSON.stringify(datainfo),
                                            'flag': flag + ""
                                        };
                                        $.ajax({
                                            type: 'post',
                                            async: true,
                                            url: "/seeyon/rest/cap4/selectPeople/backfillpeopleInfo2",
                                            dataType: 'json',
                                            data: JSON.stringify(jsonAdd),
                                            contentType: 'application/json',
                                            success: function (res) {
                                                // 判断是否需要添加
                                                if (res.code != 0) {
                                                    $.alert(res.message);
                                                    return;
                                                }
                                                var isNext = res.data.add;
                                                var val = 0;

                                                if (isNext) {
                                                    var addLineParam = {};
                                                    var dataCount = res.data.dataCount;

                                                    addLineParam.tableName = res.data.tableName;
                                                    addLineParam.isFormRecords = true;
                                                    addLineParam.callbackFn = function () {
                                                        addLineAndFilldata(content, adaptation, messageObj, privateId, peoples, flag);
                                                    }
                                                    window.thirdPartyFormAPI.insertFormsonRecords(addLineParam);
                                                } else {
                                                    var dataList = res.data.data;
                                                    if (null != dataList && dataList != '') {
                                                        for (var i = 0; i < dataList.length; i++) {
                                                            var backfill = {};
                                                            backfill.tableName = res.data.tableName;
                                                            backfill.tableCategory = "formson";
                                                            backfill.updateData = dataList[i][dataList[i].recordId];
                                                            backfill.updateRecordId = dataList[i].recordId;
                                                            adaptation.backfillFormControlData(backfill, privateId);

                                                        }
                                                    }

                                                }

                                            }
                                        });
                                    }

                                    dialog.close();
                                }
                            });
                        }

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
