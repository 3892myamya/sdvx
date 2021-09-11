var getURLParams = function(path) {
    if (!path) return false;
    var param = path.match(/\?([^?]*)$/);
    if (!param || param[1] === '') return false;
    var tmpParams = param[1].split('&');
    var keyValue  = [];
    var params    = {};
    for (var i = 0, len = tmpParams.length; i < len; i++) {
        keyValue = tmpParams[i].split('=');
        params[keyValue[0]] = keyValue[1];
    }
    return params;
};

$(function() {
    $('#btn_submit').on('click', function(event){
        if ($('#caption').text() == '処理中です。少々お待ちください……'){
            // TODO 強引か…？
            return;
        }
        $('#div_result').children().remove();
        $('#caption').text('処理中です。少々お待ちください……');
        $('#loading').show();
        var param = {};
        param.url = $('#edt_url').val();
        if ($('#edt_url').val().indexOf('penpa-edit') > -1) {
            // pを復号
            var param = getURLParams($('#edt_url').val())['p'];
            var ab = atob(param);
            ab = Uint8Array.from(ab.split(""), e => e.charCodeAt(0));
            var inflate = new Zlib.RawInflate(ab);
            var plain = inflate.decompress();
            var rtext = new TextDecoder().decode(plain);
            param.fieldStr = rtext;
        }
        $.ajax({
            url: location.host.indexOf('localhost') == -1 ? 'https://myamyaapi.herokuapp.com/SolverWeb' : 'SolverWeb',
            type: 'POST',
            dataType: 'text',
            data: param
        }).done(function(result) {
            var resultObj = JSON.parse(result);
            $('#caption').text(resultObj.status);
            $('#div_result').html(resultObj.result);
            $('#loading').hide();
        }).fail(function() {
            $('#caption').text('通信時にエラーが発生しました');
            $('#loading').hide();
        });
    });
    $('#div_readme_head').click(function(){
        $('#div_readme_body').slideToggle();
    });
//    $('#edt_url').keyup(function(){
//        if ($('#edt_url').val().indexOf('penpa-edit') > -1){
//            $('#div_ext').show();
//        } else {
//            $('#div_ext').hide();
//        }
//    });
    $('#loading').hide();
    $('#div_ext').hide();
});
