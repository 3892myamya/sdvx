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
        param.pattern = $('#sel_pattern').val();
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
                var tweetTxt = '数独ガチャでLv' + resultObj.level + 'の問題を獲得！ ' + resultObj.url;
                var splitted = $('#tweetbtn').attr('href').split("&");
                splitted[1] ='text=' + encodeURIComponent(tweetTxt);
                $('#tweetbtn').attr('href', splitted.join('&'))
                $('#tweetbtn').show();
                $('#edt_if').val(resultObj.txt);
                $('#edt_if').show();
            }
        }).fail(function() {
            $('#caption').text('通信時にエラーが発生しました');
        });
    });
    $('#sel_pattern').val(1);
    $('#tweetbtn').hide();
    $('#edt_if').hide();
});
