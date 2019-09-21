$(function() {
    $('#btn_submit').on('click', function(event){
        if ($('#caption').text() == '処理中です。少々お待ちください……'){
            // TODO 強引か…？
            return;
        }
        $('#div_result').children().remove();
        $('#caption').text('処理中です。少々お待ちください……');
        var param = {};
        param.url = $('#edt_url').val();
        $.ajax({
            url: location.host.indexOf('localhost') == -1 ? 'https://myamyaapi.herokuapp.com/SolverWeb' : 'SolverWeb',
            type: 'POST',
            dataType: 'text',
            data: param
        }).done(function(result) {
            var resultObj = JSON.parse(result);
            $('#caption').text(resultObj.status);
            $('#div_result').html(resultObj.result);
        }).fail(function() {
            $('#caption').text('通信時にエラーが発生しました');
        });
    });
    $('#div_readme_head').click(function(){
        $('#div_readme_body').slideToggle();
    });
});
