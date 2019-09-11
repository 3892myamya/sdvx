var ruleMap = {
	sudoku : {name: "数独", url:"https://www.nikoli.co.jp/ja/puzzles/sudoku/",source:"ニコリ公式"},
	shakashaka : {name: "シャカシャカ", url:"https://www.nikoli.co.jp/ja/puzzles/shakashaka/",source:"ニコリ公式"},
	nurimisaki : {name: "ぬりみさき", url:"https://www.nikoli.co.jp/ja/puzzles/nurimisaki/",source:"ニコリ公式"},
	gokigen : {name: "ごきげんななめ", url:"https://www.nikoli.co.jp/ja/puzzles/gokigen_naname/",source:"ニコリ公式"},
	creek : {name: "クリーク", url:"https://www.nikoli.co.jp/ja/puzzles/creek/",source:"ニコリ公式"},
	tasquare : {name: "たすくえあ", url:"https://www.nikoli.co.jp/ja/puzzles/tasukuea/",source:"ニコリ公式"},
	reflect : {name: "リフレクトリンク", url:"http://indi.s58.xrea.com/reflect/",source:"連続発破保管庫さん"}
}

$(function() {
    $('#btn_submit').on('click', function(event){
        if ($('#caption').text() == '抽選中です。少々お待ちください……'){
            // TODO 強引か…？
            return;
        }
        $('#tweetbtn').hide();
        $('#edt_if').hide();
        $('#div_result').text('');
        $('#div_link').text('');
        $('#caption').text('抽選中です。少々お待ちください……');
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
            }
        }).fail(function() {
            $('#caption').text('通信時にエラーが発生しました');
        });
    });
    $('#sel_type').change(function(){
        showhide();
    });
    var showhide = function () {
        var type = $('#sel_type').val();
        if (type == 'sudoku') {
            $('#lbl_pattern').show();
            $('#sel_pattern').show();
            $('#lbl_size').hide();
            $('#sel_size').hide();
        } else {
            $('#lbl_pattern').hide();
            $('#sel_pattern').hide();
            $('#lbl_size').show();
            $('#sel_size').show();
        }
        var oneRule = ruleMap[type];
        if (oneRule !== undefined){
            $('#a_rule').attr('href', oneRule.url)
            $('#a_rule').text(oneRule.name + 'のルールを表示(' + oneRule.source + ')')
            $('#div_rule').show();
        } else {
            $('#div_rule').hide();
        }
    }
    // 保存された条件があれば読みだす
    var cond = localStorage.getItem('condsudoku');
    var condObj = JSON.parse(cond);
    if (condObj != null){
        $('#sel_type').val(condObj.type);
        $('#sel_pattern').val(condObj.pattern);
        $('#sel_size').val(condObj.size);
    } else {
        // 初期条件。
        $('#sel_type').val('sudoku');
        $('#sel_pattern').val(1);
        $('#sel_size').val(4);
    }
    $('#tweetbtn').hide();
    $('#edt_if').hide();
    showhide();
});
