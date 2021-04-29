$(function() {
    var isActive = false;
    var div = 0; // 1,2,3,4 ： +,-,x,/
    var level = 0;
    var count = 0;//問題数

    // ボタン配列処理
    var initArray = function() {
        $('#div_number').removeClass();
        if ($('#sel_array').val() == 2){
            $('#div_number').addClass('middlebtn');
            $('#div_number').html('<div id="div_tools"><img id="img_C" src="img/C.png"><img id="img_F" src="img/F.png"></div><div><img id="img_1" src="img/1.png"><img id="img_2" src="img/2.png"><img id="img_3" src="img/3.png"><img id="img_4" src="img/4.png"><img id="img_5" src="img/5.png"></div><div><img id="img_6" src="img/6.png"><img id="img_7" src="img/7.png"><img id="img_8" src="img/8.png"><img id="img_9" src="img/9.png"><img id="img_0" src="img/0.png"></div><div><button type="button" id="btn_answer">解答する</button></div>');
            $('#btn_answer').width('335px');
        } else if ($('#sel_array').val() == 4){
            $('#div_number').addClass('largebtn');
            $('#div_number').html('<div id="div_tools"><img id="img_C" src="img/C.png"><img id="img_F" src="img/F.png"></div><div><img id="img_7" src="img/7.png"><img id="img_8" src="img/8.png"><img id="img_9" src="img/9.png"></div><div><img id="img_4" src="img/4.png"><img id="img_5" src="img/5.png"><img id="img_6" src="img/6.png"></div><div><img id="img_1" src="img/1.png"><img id="img_2" src="img/2.png"><img id="img_3" src="img/3.png"></div><div><img id="img_0" src="img/0.png"><button type="button" id="btn_answer">解答する</button></div>');
            $('#btn_answer').width('180px');
        }
    }

    $('#sel_array').change(function() {
        initArray();
    });

    // ハイスコアグリッド表示用
    // div-level-countをキーとし、下に[順位,pts,日付]をぶら下げる。
    var hiscoreGridInfo = {};

    $('#sel_div').change(function() {
        showHiscoreGrid();
    });
    $('#sel_count').change(function() {
        showHiscoreGrid();
    });
    $('#sel_level').change(function() {
        showHiscoreGrid();
    });

    var showHiscoreGrid = function() {
        $('#lbl_hiscore').text('ハイスコア(' +$('#sel_div option:selected').text() + ':レベル' + $('#sel_level').val() + ':問題数' + $('#sel_count').val() + ')');
        $('#grid_hiscore').jsGrid({
            width : '100%',
            noDataContent : 'まだ開始していません',
            data : hiscoreGridInfo[$('#sel_div').val()][$('#sel_level').val()][$('#sel_count').val()],
            fields : [  {
                title : '順位',
                name : 'rank',
                type : 'number',
                width : 30
            }, {
                title : 'pts.',
                name : 'score',
                type : 'number',
                width : 100
            }, {
                title : '記録日時',
                name : 'record_date',
                type : 'text',
                width : 200
            }, ]
        });
    }


    //開始ボタンの処理
    $('#btn_start').on('click', function(event){
        $('#btn_start').get(0).blur();
        div = $('#sel_div').val();
        level = $('#sel_level').val();
        count = $('#sel_count').val();
        var condObj = {
            div : div,
            level : level,
            count : count,
            array : $('#sel_array').val()
        };
        var cond = JSON.stringify(condObj);
        localStorage.setItem('condsansu', cond);
        initHistoryGridInfo();
        showHistoryGrid();
        if ($('#btn_start').text() == '中断する'){
            // 終了処理
            $('#lbl_status').text('状況：開始前です');
            $('#lbl_quest').text('開始前です');
            $('#lbl_judge').removeClass();
            $('#lbl_judge').text('');
            $('#lbl_answer').text('');
            $('#btn_start').text('開始する');
            return;
        }
        var baseTime = new Date().getTime();
        var countdown = function () {
            var diff = parseInt((baseTime + 3000 - new Date().getTime()) / 10) / 100;
            if (diff > 0){
                $('#lbl_quest').text(diff + '秒前…');
                setTimeout(countdown, 50);
            } else {
                $('#lbl_quest').text('スタート！');
                setTimeout(function () {
                   $('#btn_start').text('中断する');
                   total = 0;
                   questCnt = 0;
                   makeQuest();
                }, 500);
            }
        };
        $('#lbl_status').text('状況：まもなく始まります');
        countdown();
    });

    var questCnt = 0;// 今何問目か
    var answer;// 今の問題の答え
    var point; //今の問題を解いたときに獲得するポイント。5-1
    var speed; //今の問題を解いたときに獲得するスピードボーナスの基準値
    var total = 0;

    // 回答グリッド表示用
    var historyGridInfo = [];

    var initHistoryGridInfo = function() {
        historyGridInfo.splice(0);
        for(var i = 0; i < count; i++) {
            historyGridInfo.push({
                no:i + 1
            });
        }
    }

    var showHistoryGrid = function() {
        $('#grid_history').jsGrid({
            width : '100%',
            noDataContent : 'まだ開始していません',
            data : historyGridInfo,
            fields : [  {
                title : 'No.',
                name : 'no',
                type : 'number',
                width : 30
            }, {
                title : '問題',
                name : 'quest',
                type : 'text',
                width : 100
            }, {
                title : '答え',
                name : 'answer',
                type : 'text',
                width : 50
            }, {
                title : '得点',
                name : 'score',
                type : 'number',
                width : 30
            }, {
                title : 'ボーナス',
                name : 'sp',
                type : 'number',
                width : 60
            }, ]
        });
    }

    var getNowDateStr = function() {
        var date = new Date();
        var year_str = date.getFullYear();
        var month_str = 1 + date.getMonth();
        var day_str = date.getDate();
        var hour_str = date.getHours();
        var minute_str = date.getMinutes();
        var second_str = date.getSeconds();
        var format_str = 'YYYY/MM/DD hh:mm:ss';
        format_str = format_str.replace(/YYYY/g, year_str);
        format_str = format_str.replace(/MM/g, ('0' + month_str).slice(-2));
        format_str = format_str.replace(/DD/g, ('0' + day_str).slice(-2));
        format_str = format_str.replace(/hh/g, ('0' + hour_str).slice(-2));
        format_str = format_str.replace(/mm/g, ('0' + minute_str).slice(-2));
        format_str = format_str.replace(/ss/g, ('0' + second_str).slice(-2));
        return format_str;
    };

    // 問題を抽選して表示する
    var makeQuest = function() {
        questCnt++;
        if (questCnt > count){
            // 終了処理
            var nowScore = parseInt(total * 1000)/1000;
            $('#lbl_status').text('状況：終了しました');
            $('#lbl_quest').text('得点：' + nowScore + 'pts.');
            $('#lbl_judge').removeClass();
            $('#lbl_judge').text('');
            $('#lbl_answer').text('');
            $('#btn_start').text('開始する');
            showHistoryGrid();
            // ハイスコア記録処理
            var targetHiScore = hiscoreGridInfo[div][level][count]; 
            var rank = 0;
            for(var i = 0; i < targetHiScore.length; i++) {
                if (targetHiScore[i].score == '' || targetHiScore[i].score < nowScore) {
                    rank = i + 1;
                    break;
                }
            }
            if (rank != 0){
                alert('ハイスコア' + rank + '位にランクインしました！')
                for(var i = targetHiScore.length - 2; i >= rank - 1; i--) {
                    targetHiScore[i + 1].score = targetHiScore[i].score;
                    targetHiScore[i + 1].record_date = targetHiScore[i].record_date;
                }
                targetHiScore[rank - 1].score = nowScore;
                targetHiScore[rank - 1].record_date = getNowDateStr();
                var hiscoreStr = JSON.stringify(hiscoreGridInfo);
                localStorage.setItem('hiscore', hiscoreStr);
                showHiscoreGrid();
            }
            return;
        } 
        point = 100 / count;
        speed = new Date().getTime();
        $('#lbl_judge').removeClass();
        $('#lbl_judge').text('');
        var questTxt = '';
        while (true) {
            if (level == 1){
                if (div == 1){
                    var numA = Math.floor(Math.random() * 8) + 1;
                    var numB = Math.floor(Math.random() * 8) + 1;
                    questTxt = numA + ' ＋ ' + numB + ' = ';
                    answer = numA + numB;
                } else if (div == 2){
                    var numA = Math.floor(Math.random() * 8) + 1;
                    var numB = Math.floor(Math.random() * 8) + 1;
                    answer = numA + numB;
                    questTxt = answer + ' － ' + numB + ' = ';
                    answer = numA
                } else if (div == 3){
                    var numA = Math.floor(Math.random() * 8) + 1;
                    var numB = Math.floor(Math.random() * 8) + 1;
                    questTxt = numA + ' × ' + numB + ' = ';
                    answer = numA * numB;
                } else if (div == 4){
                    var numA = Math.floor(Math.random() * 8) + 1;
                    var numB = Math.floor(Math.random() * 8) + 1;
                    answer = numA * numB;
                    questTxt = answer + ' ÷ ' + numB + ' = ';
                    answer = numA
                }
            } else if (level == 2){
                if (div == 1){
                    var numA = Math.floor(Math.random() * 98) + 1;
                    var numB = Math.floor(Math.random() * 98) + 1;
                    questTxt = numA + ' ＋ ' + numB + ' = ';
                    answer = numA + numB;
                } else if (div == 2){
                    var numA = Math.floor(Math.random() * 98) + 1;
                    var numB = Math.floor(Math.random() * 98) + 1;
                    answer = numA + numB;
                    questTxt = answer + ' － ' + numB + ' = ';
                    answer = numA
                } else if (div == 3){
                    var numA = Math.floor(Math.random() * 97) + 2;
                    var numB = Math.floor(Math.random() * 7) + 2;
                    questTxt = numA + ' × ' + numB + ' = ';
                    answer = numA * numB;
                } else if (div == 4){
                    var numA = Math.floor(Math.random() * 97) + 2;
                    var numB = Math.floor(Math.random() * 7) + 2;
                    answer = numA * numB;
                    questTxt = answer + ' ÷ ' + numB + ' = ';
                    answer = numA
                }
            } else if (level == 3){
                if (div == 1){
                    var numA = Math.floor(Math.random() * 998) + 1;
                    var numB = Math.floor(Math.random() * 998) + 1;
                    questTxt = numA + ' ＋ ' + numB + ' = ';
                    answer = numA + numB;
                } else if (div == 2){
                    var numA = Math.floor(Math.random() * 998) + 1;
                    var numB = Math.floor(Math.random() * 998) + 1;
                    answer = numA + numB;
                    questTxt = answer + ' － ' + numB + ' = ';
                    answer = numA
                } else if (div == 3){
                    var numA = Math.floor(Math.random() * 97) + 2;
                    var numB = Math.floor(Math.random() * 97) + 2;
                    questTxt = numA + ' × ' + numB + ' = ';
                    answer = numA * numB;
                } else if (div == 4){
                    var numA = Math.floor(Math.random() * 97) + 2;
                    var numB = Math.floor(Math.random() * 97) + 2;
                    answer = numA * numB;
                    questTxt = answer + ' ÷ ' + numB + ' = ';
                    answer = numA
                }
            }
            if (questTxt != $('#lbl_quest').text()) {
                // 同じ問題が連続しないよう再抽選する処理
                break;
            }
        }
        $('#lbl_status').text('状況：' + questCnt + '/' + count + '問目');
        $('#lbl_quest').text(questTxt);
        $('#lbl_answer').text('');
        historyGridInfo[questCnt - 1].quest = questTxt;
        showHistoryGrid();
        isActive = true;
    }

    // 解答ボタンを押したときの処理
    var pressAnswer = function() {
        if (isActive){
            $('#btn_answer').get(0).blur();
            isActive = false;
            if (answer == $('#lbl_answer').text()) {
                 $('#lbl_judge').addClass('ok');
                 $('#lbl_judge').text('○');
                 historyGridInfo[questCnt - 1].answer = answer;
                 historyGridInfo[questCnt - 1].score = point;
                 var sp = parseInt(((10000 * level) - (new Date().getTime() - speed))/count/level/10)/ 1000;
                 if (sp < 0) {
                     sp = 0;
                 }
                 historyGridInfo[questCnt - 1].sp = sp;
                 total = total + point + sp;
                 setTimeout(function () {
                     makeQuest();
                }, 500);
            } else {
                $('#lbl_judge').addClass('ng');
                $('#lbl_judge').text('×');
                if (point > 1){
                    point--;
                }
                setTimeout(function () {
                    $('#lbl_answer').text('');
                    $('#lbl_judge').removeClass();
                    $('#lbl_judge').text('');
                    isActive = true;
                }, 500);
            }
        }
    }

    // 白旗ボタンを押したときの処理
    var giveUp = function() {
        if (isActive){
            isActive = false;
            historyGridInfo[questCnt - 1].answer = answer;
            historyGridInfo[questCnt - 1].score = 0;
            historyGridInfo[questCnt - 1].sp = 0;
            showHistoryGrid();
            $('#lbl_answer').text(answer);
            setTimeout(function () {
                $('#lbl_answer').text('');
                setTimeout(function () {
                    $('#lbl_answer').text(answer);
                    setTimeout(function () {
                        makeQuest();
                    }, 1000);
                }, 500)
            }, 500)
        }
    }

    // 数字ボタンクリック時の処理、0.1秒だけ画像を変える
    var numberButton = function(key) {
       if (isActive){
           $('#img_' + key).attr('src','img/' + key + 'p.png');
           if (key == 'F') {
               // 白旗ボタン向けの特殊処理でやる
           } else if (key == 'C') {
               $('#lbl_answer').text('');
           } else if (key == '0' && $('#lbl_answer').text() == '') {
               // 0は空打ちできない
           } else if ($('#lbl_answer').text().length >= 4) {
               // 4文字以上は入力できない
           } else {
               $('#lbl_answer').text($('#lbl_answer').text() + key);
           }
           setTimeout(function () {
               $('#img_' + key).attr('src','img/' + key + '.png');
           }, 100);
           if (key == 'F') {
               giveUp();
           }
       }
    }

    $('body').on('keydown', function(e) {
        if (e.keyCode == 8){
            if (isActive){
                $('#lbl_answer').text($('#lbl_answer').text().slice(0,-1));
            }
        }
    });

    $('body').on('keypress', function(e) {
        if (e.keyCode == 48){
            numberButton(0);
        } else if (e.keyCode == 49){
            numberButton(1);
        } else if (e.keyCode == 50){
            numberButton(2);
        } else if (e.keyCode == 51){
            numberButton(3);
        } else if (e.keyCode == 52){
            numberButton(4);
        } else if (e.keyCode == 53){
            numberButton(5);
        } else if (e.keyCode == 54){
            numberButton(6);
        } else if (e.keyCode == 55){
            numberButton(7);
        } else if (e.keyCode == 56){
            numberButton(8);
        } else if (e.keyCode == 57){
            numberButton(9);
        } else if (e.keyCode == 13){
            pressAnswer();
        }
    });
    $('body').on('click', '#img_1' , function(e) {
        numberButton(1);
    });
    $('body').on('click', '#img_2' , function(e) {
        numberButton(2);
    });
    $('body').on('click', '#img_3' , function(e) {
        numberButton(3);
    });
    $('body').on('click', '#img_4' , function(e) {
        numberButton(4);
    });
    $('body').on('click', '#img_5' , function(e) {
        numberButton(5);
    });
    $('body').on('click', '#img_6' , function(e) {
        numberButton(6);
    });
    $('body').on('click', '#img_7' , function(e) {
        numberButton(7);
    });
    $('body').on('click', '#img_8' , function(e) {
        numberButton(8);
    });
    $('body').on('click', '#img_9' , function(e) {
        numberButton(9);
    });
    $('body').on('click', '#img_0' , function(e) {
        numberButton(0);
    });
    $('body').on('click', '#img_C' , function(e) {
        numberButton('C');
    });
    $('body').on('click', '#img_F' , function(e) {
        numberButton('F');
    });
    $('body').on('click', '#btn_answer' , function(e) {
        pressAnswer();
    });
    $('#btn_hiscore_clear').on('click', function(event) {
        if (window.confirm('表示中のハイスコアをクリアします。よろしいですか？')) {
            var wkDiv = $('#sel_div').val();
            var wkLevel = $('#sel_level').val();
            var wkCount = $('#sel_count').val();
            hiscoreGridInfo[wkDiv][wkLevel][wkCount] = [];
            for(var i = 0; i < 5; i++) {
                hiscoreGridInfo[wkDiv][wkLevel][wkCount][i] = {};
                hiscoreGridInfo[wkDiv][wkLevel][wkCount][i].rank = i + 1;
                hiscoreGridInfo[wkDiv][wkLevel][wkCount][i].score = '';
                hiscoreGridInfo[wkDiv][wkLevel][wkCount][i].record_date = '';
            }
            var hiscoreStr = JSON.stringify(hiscoreGridInfo);
            localStorage.setItem('hiscore', hiscoreStr);
            showHiscoreGrid();
        }
    });
    // 保存された条件があれば読みだす
    var cond = localStorage.getItem('condsansu');
    var condObj = JSON.parse(cond);
    if (condObj != null) {
        if (condObj.div === undefined) {
            $('#sel_div').val(1);
        } else {
            $('#sel_div').val(condObj.div);
        }
        if (condObj.level === undefined) {
            $('#sel_level').val(1);
        } else {
            $('#sel_level').val(condObj.level);
        }
        if (condObj.count === undefined) {
            $('#sel_count').val(10);
        } else {
            $('#sel_count').val(condObj.count);
        }
        if (condObj.array === undefined) {
            $('#sel_array').val(2);
        } else {
            $('#sel_array').val(condObj.array);
        }
    }
    var hiscore = localStorage.getItem('hiscore');
    hiscoreGridInfo = JSON.parse(hiscore);
    if (hiscoreGridInfo == null) {
        hiscoreGridInfo = {}
        $("#sel_div option").each(function(){
            var wkDiv = $(this).val();
            hiscoreGridInfo[wkDiv] = {};
            $("#sel_level option").each(function(){
                var wkLevel = $(this).val();
                hiscoreGridInfo[wkDiv][wkLevel] = {};
                $("#sel_count option").each(function(){
                    var wkCount = $(this).val();
                    hiscoreGridInfo[wkDiv][wkLevel][wkCount] = [];
                    for(var i = 0; i < 5; i++) {
                        hiscoreGridInfo[wkDiv][wkLevel][wkCount][i] = {};
                        hiscoreGridInfo[wkDiv][wkLevel][wkCount][i].rank = i+1;
                        hiscoreGridInfo[wkDiv][wkLevel][wkCount][i].score = '';
                        hiscoreGridInfo[wkDiv][wkLevel][wkCount][i].record_date = '';
                    }
                });
            });
        });
    }
    showHiscoreGrid();
    initArray();
});
