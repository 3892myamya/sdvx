$(function() {
    $('#btn_submit').on('click', function(event){
        if ($('#caption').text() == '処理中です。少々お待ちください……'){
            // TODO 強引か…？
            return;
        }
        $('#tweetbtn').hide();
        $('#grid').empty();
        $('#achieve_info').text('');
        $('#caption').text('処理中です。少々お待ちください……');
        var param = {};
        param.userid = $('#edt_userid').val();
        param.mode = $('#sel_mode').val();
        param.lvl_min = $('#sel_lvl_min').val();
        param.lvl_max = $('#sel_lvl_max').val();
        param.disp_cnt = $('#sel_disp_cnt').val();
        param.border = $('#sel_border').val();
        param.clear = $('#sel_clear').val();
        // 条件をローカルストレージ保存
        var cond = JSON.stringify(param);
        localStorage.setItem('cond', cond);
        $.ajax({
            url: location.host.indexOf('localhost') == -1 ? 'https://myamyaapi.herokuapp.com/KadaiGeneratorWeb' : 'KadaiGeneratorWeb',
            type: 'POST',
            dataType: 'text',
            data: param
        }).done(function(result) {
            var resultObj = JSON.parse(result);
            $('#caption').text(resultObj.error_msg);
            if (resultObj.error_msg == ''){
                if (param.mode == 4 ) {
                    $('#achieve_info').text(' 到達率: ' + resultObj.achieve_info);
                } else if (param.mode == 5 ) {
                    $('#achieve_info').text(' 達成率: ' + resultObj.achieve_info);
                }
                var tweetTxt = '私の';
                if (param.mode != 4 && param.mode != 5) {
                    var baseInfo = resultObj.result[0];
                    tweetTxt = tweetTxt + 'Lv' + baseInfo.level;
                    if (param.mode == 1) {
                        tweetTxt = tweetTxt + 'の課題曲は ';
                    } else if (param.mode == 2) {
                        tweetTxt = tweetTxt + 'の武器曲は ';
                    } else if (param.mode == 3) {
                        tweetTxt = tweetTxt + 'で' + $('#sel_clear option[value=' + param.clear +']').text() + 'が達成できそうな曲は';
                    } else if (param.mode == 6) {
                        tweetTxt = tweetTxt + 'でのVOLFORCE更新できそうな曲は ';
                    }
                    tweetTxt = tweetTxt + baseInfo.title + '(' + baseInfo.effect_div + ')';
                    tweetTxt = tweetTxt + ' です。'
                    if (param.mode != 3 ) {
                    	if (param.mode == 6){
                    		tweetTxt = tweetTxt + '(目標:' + baseInfo.score + ')';
                    	} else {
                    		tweetTxt = tweetTxt + '(' + baseInfo.score + ':上位' + baseInfo.estimate_rate + ')';
                    	}
                    }
                } else {
                    tweetTxt = tweetTxt + 'Lv' + param.lvl_min + 'の';
                    if (param.mode == 4 ) {
                        tweetTxt = tweetTxt + param.border +'%ボーダー達成状況は ';
                    } else {
                        tweetTxt = tweetTxt + $('#sel_clear option[value=' + param.clear +']').text() + '達成状況は ';
                    }
                    tweetTxt = tweetTxt + resultObj.achieve_info;
                    tweetTxt = tweetTxt + ' です。'
                }
                $('#tweetbtn').show();
                var splitted = $('#tweetbtn').attr('href').split("&");
                splitted[1] ='text=' + encodeURIComponent(tweetTxt);
                $('#tweetbtn').attr('href', splitted.join('&'))
                $('#grid').jsGrid({
                    width: '100%',
                    sorting: true,
                    data: resultObj.result,
                    fields: [
                        {title:'No.', name:'rank', type: 'number', width: 30},
                        {title:'曲名', name:'title', type: 'text', width: 132 },
                        {title:'譜面', name:'effect_div', type: 'text', width: 33
                        ,itemTemplate:
                            function(value, item) {
                                return "<div class='" + value.toLowerCase() + "'>" + value + "</div>";
                            }
                        },
                        {title:'Lv', name:'level', type: 'number', width: 20 },
                        {title:param.mode == 6 ? '目標' :
                        	   param.mode == 5 ? $('#sel_clear option[value=' + param.clear +']').text() + '率' :
                               param.mode == 4 ? 'ボーダー' : 'スコア', name:'score', type: 'number', width: 58
                        ,sorter:
                            function(s1, s2) {
                                return Number(s1.replace('%','')) - Number(s2.replace('%',''))
                            }
                        },
                        {title:param.mode == 5 ? '状況' : param.mode == 4 ? '差分' : (param.mode == 3 || param.mode == 6) ? '推奨度' : '上位', name:'estimate_rate',
                               type: param.mode == 5 ? 'text' : 'number', width: 50, align: 'right'
                        ,itemTemplate:
                            function(value, item) {
                               if (param.mode == 4) {
                                   if (item.estimate_rate < 0) {
                                       return "<div style='color:red'>" + value + "</div>";
                                   } else {
                                       return value;
                                   }
                               } else if (param.mode == 5) {
                                   if (value == '未達成') {
                                       return "<div style='color:red'>" + value + "</div>";
                                   } else {
                                       return value;
                                   }
                               } else {
                                   return value;
                               }
                            }
                        }
                    ]
                });
            }
        }).fail(function() {
            $('#caption').text('通信時にエラーが発生しました');
        });
    });
    $('#div_readme_head').click(function(){
        $('#div_readme_body').slideToggle();
    });
    $('#sel_mode').change(function(){
        showhide();
    });
    $('#sel_lvl_min').change(function(){
        var newVal = $('#sel_lvl_min').val();
        if (parseInt(newVal, 10) > parseInt($('#sel_lvl_max').val(), 10)) {
            $('#sel_lvl_max').val(newVal);
        }
    });
    $('#sel_lvl_max').change(function(){
        var newVal = $('#sel_lvl_max').val();
        if (parseInt(newVal, 10) < parseInt($('#sel_lvl_min').val(), 10)) {
            $('#sel_lvl_min').val(newVal);
        }
    });
    var showhide = function () {
        var mode = $('#sel_mode').val();
        if (mode == 3) {
            $('#lbl_lvl_kara').show();
            $('#sel_lvl_max').show();
            $('#lbl_disp_cnt').show();
            $('#sel_disp_cnt').show();
            $('#lbl_border').hide();
            $('#sel_border').hide();
            $('#lbl_clear').show();
            $('#sel_clear').show();
        } else if (mode == 4) {
            $('#lbl_lvl_kara').hide();
            $('#sel_lvl_max').hide();
            $('#lbl_disp_cnt').hide();
            $('#sel_disp_cnt').hide();
            $('#lbl_border').show();
            $('#sel_border').show();
            $('#lbl_clear').hide();
            $('#sel_clear').hide();
        } else if (mode == 5) {
            $('#lbl_lvl_kara').hide();
            $('#sel_lvl_max').hide();
            $('#lbl_disp_cnt').hide();
            $('#sel_disp_cnt').hide();
            $('#lbl_border').hide();
            $('#sel_border').hide();
            $('#lbl_clear').show();
            $('#sel_clear').show();
        } else {
            $('#lbl_lvl_kara').show();
            $('#sel_lvl_max').show();
            $('#lbl_disp_cnt').show();
            $('#sel_disp_cnt').show();
            $('#lbl_border').hide();
            $('#sel_border').hide();
            $('#lbl_clear').hide();
            $('#sel_clear').hide();
        }
        $('#tweetbtn').hide();
        $('#achieve_info').text('');
    }

    // 保存された条件があれば読みだす
    var cond = localStorage.getItem('cond');
    var condObj = JSON.parse(cond);
    if (condObj != null){
        $('#edt_userid').val(condObj.userid);
        $('#sel_lvl_min').val(condObj.lvl_min);
        $('#sel_lvl_max').val(condObj.lvl_max);
        $('#sel_mode').val(condObj.mode);
        $('#sel_disp_cnt').val(condObj.disp_cnt);
        if (condObj.border != null) {
            $('#sel_border').val(condObj.border);
        } else {
            $('#sel_border').val(50);
        }
        if (condObj.clear != null) {
            $('#sel_clear').val(condObj.clear);
        } else {
            $('#sel_clear').val(2);
        }
    } else {
        // 初期条件。おそらく一番使われそうである値を入れておく
        $('#sel_lvl_min').val(17);
        $('#sel_lvl_max').val(20);
        $('#sel_mode').val(1);
        $('#sel_disp_cnt').val(20);
        $('#sel_border').val(50);
        $('#sel_clear').val(2);
    }
    showhide();
});
