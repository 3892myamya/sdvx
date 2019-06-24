$(function() {
    $('#btn_submit').on('click', function(event){
        if ($('#caption').text() == '処理中です。少々お待ちください……'){
            // TODO 強引か…？
            return;
        }
        $('#div_result').text('');
        $('#caption').text('処理中です。少々お待ちください……');
        var param = {};
        param.cnt = $('#sel_cnt').val();
        param.height = $('#sel_height').val();
        param.width = $('#sel_width').val();
        param.right = $('#cb_right').prop('checked');
        param.up = $('#cb_up').prop('checked');
        param.down = $('#cb_down').prop('checked');
        param.left = $('#cb_left').prop('checked');
        $.ajax({
            url: 'HeyaWeb',
            type: 'POST',
            dataType: 'text',
            data: param
        }).done(function(result) {
            var resultObj = JSON.parse(result);
            $('#caption').text(resultObj.status);
            $('#div_result').text(resultObj.result);
        }).fail(function() {
            $('#caption').text('通信時にエラーが発生しました');
        });
    });

    $('#sel_cnt').val(5);
    $('#sel_height').val(4);
    $('#sel_width').val(3);
});
