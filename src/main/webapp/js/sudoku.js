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
        param.height = $('#sel_size').val();
        param.width = $('#sel_size').val();
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
        $('#tweetbtn').hide();
        $('#edt_if').hide();
    }
    $('#sel_type').val('sudoku');
    $('#sel_pattern').val(1);
    $('#lbl_size').hide();
    $('#sel_size').hide();
    $('#tweetbtn').hide();
    $('#edt_if').hide();
});
