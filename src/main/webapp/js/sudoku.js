var ruleMap = {
	sudoku : {name: "数独", url:"https://www.nikoli.co.jp/ja/puzzles/sudoku/",source:"ニコリ公式"},
	shakashaka : {name: "シャカシャカ", url:"https://www.nikoli.co.jp/ja/puzzles/shakashaka/",source:"ニコリ公式"},
	nurimisaki : {name: "ぬりみさき", url:"https://www.nikoli.co.jp/ja/puzzles/nurimisaki/",source:"ニコリ公式"},
	gokigen : {name: "ごきげんななめ", url:"https://www.nikoli.co.jp/ja/puzzles/gokigen_naname/",source:"ニコリ公式"},
	creek : {name: "クリーク", url:"https://www.nikoli.co.jp/ja/puzzles/creek/",source:"ニコリ公式"},
	tasquare : {name: "たすくえあ", url:"https://www.nikoli.co.jp/ja/puzzles/tasukuea/",source:"ニコリ公式"},
	reflect : {name: "リフレクトリンク", url:"http://indi.s58.xrea.com/reflect/",source:"連続発破保管庫さん"},
	akari : {name: "美術館", url:"https://www.nikoli.co.jp/ja/puzzles/akari/",source:"ニコリ公式"},
	slither : {name: "スリザーリンク", url:"https://www.nikoli.co.jp/ja/puzzles/slitherlink/",source:"ニコリ公式"},
	tapa : {name: "Tapa", url:"http://indi.s58.xrea.com/tapa/",source:"連続発破保管庫さん"},
	sashigane : {name: "さしがね", url:"https://www.nikoli.co.jp/ja/puzzles/sashigane/",source:"ニコリ公式"},
	masyu : {name: "ましゅ", url:"https://www.nikoli.co.jp/ja/puzzles/masyu/",source:"ニコリ公式"},
	geradeweg : {name: "グラーデヴェグ", url:"",source:""},
	bag : {name: "バッグ", url:"https://www.nikoli.co.jp/ja/puzzles/bag/",source:"ニコリ公式"},
	kurodoko : {name: "黒どこ", url:"https://www.nikoli.co.jp/ja/puzzles/where_is_black_cells/",source:"ニコリ公式"},
	barns : {name: "バーンズ", url:"http://indi.s58.xrea.com/barns/",source:"連続発破保管庫さん"},
	midloop : {name: "ミッドループ", url:"https://www.nikoli.co.jp/ja/puzzles/mid-loop/",source:"ニコリ公式"},
	sukoro : {name: "数コロ", url:"https://www.nikoli.co.jp/ja/puzzles/sukoro/",source:"ニコリ公式"},
	balance : {name: "バランスループ", url:"",source:""},
	minarism : {name: "マイナリズム", url:"	http://indi.s58.xrea.com/minarism/",source:"連続発破保管庫さん"},
	box : {name: "ボックス", url:"",source:""},
	kurotto : {name: "クロット", url:"https://www.nikoli.co.jp/ja/puzzles/kurotto/",source:"ニコリ公式"},
	tents : {name: "Tents", url:"",source:""},
	walllogic : {name: "ウォールロジック", url:"https://ja.wikipedia.org/wiki/%E3%82%A6%E3%82%A9%E3%83%BC%E3%83%AB%E3%83%AD%E3%82%B8%E3%83%83%E3%82%AF",source:"Wikipedia"},
	nurikabe : {name: "ぬりかべ", url:"https://www.nikoli.co.jp/ja/puzzles/nurikabe/",source:"ニコリ公式"},
	simpleloop : {name: "シンプルループ", url:"",source:""},
	yinyang : {name: "しろまるくろまる", url:"",source:""},
}

var option = {
	size_3 : '<option value="3">3 x 3</option>',
	size_4 : '<option value="4">4 x 4</option>',
	size_5 : '<option value="5">5 x 5</option>',
	size_6 : '<option value="6">6 x 6</option>',
	size_7 : '<option value="7">7 x 7</option>',
	size_8 : '<option value="8">8 x 8</option>',
	size_9 : '<option value="9">9 x 9</option>',
	size_10 : '<option value="10">10 x 10</option>',
	pattern_0: '<option value="0">フリー</option>',
	pattern_1: '<option value="1">点対称</option>',
	pattern_2: '<option value="2">左右対称</option>',
	pattern_3: '<option value="3">上下対称</option>',
	pattern_4: '<option value="4">＼対称</option>',
	pattern_5: '<option value="5">／対称</option>',
	pattern_6: '<option value="6">上下左右対称</option>',
	pattern_7: '<option value="7">卍型</option>',
}

$(function() {
    var getTime = function(){
    	//先頭ゼロ付加
    	var padZero = function(num) {
    		var result;
    		if (num < 10) {
    			result = "0" + num;
    		} else {
    			result = "" + num;
    		}
    		return result;
    	}
    	var now = new Date();
    	var time = "" + now.getFullYear() + "/" + padZero(now.getMonth() + 1) +
    		"/" + padZero(now.getDate()) + " " + padZero(now.getHours()) + ":" +
    		padZero(now.getMinutes()) + ":" + padZero(now.getSeconds());
    	return time;
    }
	$('#btn_clear').on('click', function(event){
		if(window.confirm('履歴をクリアします。よろしいですか？')){
	        localStorage.setItem('history', JSON.stringify([]));
            showGrid();
		}
	});


	$('#btn_submit').on('click', function(event){
        if ($('#caption').text() == '抽選中です。少々お待ちください……'){
            // TODO 強引か…？
            return;
        }
    	var time = getTime();
        $('#tweetbtn').hide();
        $('#edt_if').hide();
        $('#div_result').text('');
        $('#div_link').text('');
        $('#caption').text('抽選中です。少々お待ちください……');
        $('#loading').show();
        var param = {};
        param.type = $('#sel_type').val();
        param.pattern = $('#sel_pattern').val();
        param.size = $('#sel_size').val();
        param.height = $('#sel_size').val();
        param.width = $('#sel_size').val();
        // 条件をローカルストレージ保存
        var cond = JSON.stringify(param);
        localStorage.setItem('condsudoku', cond);
        $.ajax({
            url: 'SudokuGacha',
            type: 'POST',
            dataType: 'text',
            data: param
        }).done(function(result) {
            var resultObj = JSON.parse(result);
            $('#caption').text(resultObj.status);
            $('#loading').hide();
            if (resultObj.result != ''){
                $('#div_result').html(resultObj.result);
                $('#div_link').html(resultObj.link);
                var tweetTxt = $('#sel_type option[value=' + param.type +']').text() + 'でLv' + resultObj.level + 'の問題を獲得！ ' + resultObj.url;
                var splitted = $('#tweetbtn').attr('href').split("&");
                splitted[1] ='text=' + encodeURIComponent(tweetTxt);
                $('#tweetbtn').attr('href', splitted.join('&'))
                $('#tweetbtn').show();
                if (resultObj.txt){
                    $('#edt_if').val(resultObj.txt);
                    $('#edt_if').show();
            	}
                updateHistory(time, param.type, resultObj);
                showGrid();
            }
        }).fail(function() {
            $('#caption').text('通信時にエラーが発生しました');
            $('#loading').hide();
        });
    });
    $('#sel_type').change(function(){
        showhide();
    });
    $('#sel_size').change(function(){
        showhide();
    });
    var showhide = function () {
        var type = $('#sel_type').val();
        if (type == 'sudoku' || type == 'akari' || type == 'slither' || type == 'creek'
        	|| type == 'gokigen' || type == 'tapa' || type == 'bag' || type == 'kurodoko'
        	|| type == 'sukoro' || type == 'walllogic') {
            $('#lbl_pattern').show();
            $('#sel_pattern').show();
        } else {
            $('#lbl_pattern').hide();
            $('#sel_pattern').hide();
        }
        // この辺の書き方がいかにもJQuery的で古くさい…モダンFW使いたい…
        var nowSelSizeVal = $('#sel_size').val();
        $('#sel_size').empty();
        if (type == 'sudoku'){
            $('#sel_size').append(option.size_4);
            $('#sel_size').append(option.size_6);
            $('#sel_size').append(option.size_9);
            if (nowSelSizeVal < 6){
            	$('#sel_size').val(4);
            } else if (nowSelSizeVal < 9){
            	$('#sel_size').val(6);
            } else {
            	$('#sel_size').val(9);
            }
        } else if (type == 'minarism'){
            $('#sel_size').append(option.size_3);
            $('#sel_size').append(option.size_4);
            $('#sel_size').append(option.size_5);
            $('#sel_size').append(option.size_6);
            $('#sel_size').append(option.size_7);
            if (nowSelSizeVal > 7){
            	$('#sel_size').val(7);
            } else {
               	$('#sel_size').val(nowSelSizeVal);
            }
        } else if (type == 'balance' || type == 'kurotto' || type == 'nurikabe' ){
            $('#sel_size').append(option.size_3);
            $('#sel_size').append(option.size_4);
            $('#sel_size').append(option.size_5);
            $('#sel_size').append(option.size_6);
            $('#sel_size').append(option.size_7);
            $('#sel_size').append(option.size_8);
            if (nowSelSizeVal > 8){
            	$('#sel_size').val(8);
            } else {
               	$('#sel_size').val(nowSelSizeVal);
            }
        } else if (type == 'box'){
            $('#sel_size').append(option.size_3);
            $('#sel_size').append(option.size_4);
            $('#sel_size').append(option.size_5);
            $('#sel_size').append(option.size_6);
            $('#sel_size').append(option.size_7);
            $('#sel_size').append(option.size_8);
            $('#sel_size').append(option.size_9);
            if (nowSelSizeVal > 9){
            	$('#sel_size').val(9);
            } else {
               	$('#sel_size').val(nowSelSizeVal);
            }
        } else if (type == 'sashigane' || type == 'barns'){
            $('#sel_size').append(option.size_4);
            $('#sel_size').append(option.size_5);
            $('#sel_size').append(option.size_6);
            $('#sel_size').append(option.size_7);
            $('#sel_size').append(option.size_8);
            $('#sel_size').append(option.size_9);
            $('#sel_size').append(option.size_10);
            if (nowSelSizeVal == 3){
            	$('#sel_size').val(4);
            } else {
               	$('#sel_size').val(nowSelSizeVal);
            }
        } else {
            $('#sel_size').append(option.size_3);
            $('#sel_size').append(option.size_4);
            $('#sel_size').append(option.size_5);
            $('#sel_size').append(option.size_6);
            $('#sel_size').append(option.size_7);
            $('#sel_size').append(option.size_8);
            $('#sel_size').append(option.size_9);
            $('#sel_size').append(option.size_10);
        	$('#sel_size').val(nowSelSizeVal);
        }
        var nowSelPetternVal = $('#sel_pattern').val();
        $('#sel_pattern').empty();
        if (type == 'tapa' || type == 'bag'){
        	$('#sel_pattern').append(option.pattern_0);
        	$('#sel_pattern').append(option.pattern_1);
        	$('#sel_pattern').append(option.pattern_2);
        	$('#sel_pattern').append(option.pattern_3);
        	$('#sel_pattern').append(option.pattern_4);
        	$('#sel_pattern').append(option.pattern_5);
            if (nowSelPetternVal== 6){
            	$('#sel_pattern').val(2);
            } else if (nowSelPetternVal == 7){
            	$('#sel_pattern').val(1);
            } else {
               	$('#sel_pattern').val(nowSelPetternVal);
            }
        } else if (type == 'sukoro'){
        	$('#sel_pattern').append(option.pattern_0);
        	if (nowSelSizeVal <= 8) {
            	$('#sel_pattern').append(option.pattern_1);
            	$('#sel_pattern').append(option.pattern_2);
            	$('#sel_pattern').append(option.pattern_3);
            	$('#sel_pattern').append(option.pattern_4);
            	$('#sel_pattern').append(option.pattern_5);
            	if (nowSelPetternVal== 6){
            		$('#sel_pattern').val(2);
            	} else if (nowSelPetternVal == 7){
            		$('#sel_pattern').val(1);
            	} else {
            		$('#sel_pattern').val(nowSelPetternVal);
            	}
        	} else {
        		$('#sel_pattern').val(0);
        	}
        } else {
        	$('#sel_pattern').append(option.pattern_0);
        	$('#sel_pattern').append(option.pattern_1);
        	$('#sel_pattern').append(option.pattern_2);
        	$('#sel_pattern').append(option.pattern_3);
        	$('#sel_pattern').append(option.pattern_4);
        	$('#sel_pattern').append(option.pattern_5);
        	$('#sel_pattern').append(option.pattern_6);
        	$('#sel_pattern').append(option.pattern_7);
          	$('#sel_pattern').val(nowSelPetternVal);
        }
        var oneRule = ruleMap[type];
        if (oneRule.url != ''){
            $('#a_rule').attr('href', oneRule.url)
            $('#a_rule').text(oneRule.name + 'のルールを表示(' + oneRule.source + ')')
            $('#div_rule').show();
        } else {
            $('#div_rule').hide();
        }
    }
    var updateHistory = function(time, type, resultObj){
        var history = localStorage.getItem('history');
        var historyObj = JSON.parse(history);
    	if (historyObj == null){
    		historyObj = [];
    	}
    	historyObj.unshift({
    		time:time,
    		type:ruleMap[type].name,
    		level:resultObj.level,
    		link:resultObj.link.replace('puzz\.linkで','').replace('ぱずぷれv3で',''),
    	});
    	if (historyObj.length > 20 ){
    		historyObj.pop();
    	}
        localStorage.setItem('history', JSON.stringify(historyObj));

    }
    var showGrid = function(){
        var history = localStorage.getItem('history');
        var historyObj = JSON.parse(history);
    	if (historyObj == null){
    		historyObj = [];
    	}
	    $('#grid').jsGrid({
	         width: '100%',
	         noDataContent: '履歴がありません',
	         data: historyObj,
	         fields: [
	        	    {title:'回した日時', name:'time', type: 'text', width: 130 },
	        	    {title:'パズル名', name:'type', type: 'text', width: 115 },
	        	    {title:'Lv', name:'level', type: 'number', width: 30 },
	        	    {title:'リンク', name:'link', type: 'text', width: 50 },
            ]
       });
    }
    // 保存された条件があれば読みだす
    var cond = localStorage.getItem('condsudoku');
    var condObj = JSON.parse(cond);
    if (condObj != null){
        $('#sel_type').val(condObj.type);
        $('#sel_pattern').val(condObj.pattern);
        $('#sel_size').val(condObj.size);
        showGrid();
    } else {
        // 初期条件。
        $('#sel_type').val('sudoku');
        $('#sel_pattern').val(1);
        $('#sel_size').val(4);
    }
    $('#tweetbtn').hide();
    $('#edt_if').hide();
    $('#loading').hide();
    showhide();
});
