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
            param.type = $('#sel_type').val();
            var urlp = getURLParams($('#edt_url').val())['p'];
            var ab = atob(urlp);
            ab = Uint8Array.from(ab.split(""), e => e.charCodeAt(0));
            var inflate = new Zlib.RawInflate(ab);
            var plain = inflate.decompress();
            var rtext = new TextDecoder().decode(plain);
            param.fieldStr = rtext;
        }
		// 条件をローカルストレージ保存
		var cond = localStorage.getItem('condyajilin');
		var condObj = JSON.parse(cond);
		if (condObj != null) {
			condObj.type = param.type ;
		} else {
			condObj = {
					type : param.type ,
			};
		}
		cond = JSON.stringify(condObj);
		localStorage.setItem('condyajilin', cond);
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
    $('#edt_url').keyup(function(){
        if ($('#edt_url').val().indexOf('penpa-edit') > -1){
            $('#div_ext').show();
        } else {
            $('#div_ext').hide();
        }
    });
    $('#edt_url').keydown(function(){
        if ($('#edt_url').val().indexOf('penpa-edit') > -1){
            $('#div_ext').show();
        } else {
            $('#div_ext').hide();
        }
    });
    $('#edt_url').keypress(function(){
        if ($('#edt_url').val().indexOf('penpa-edit') > -1){
            $('#div_ext').show();
        } else {
            $('#div_ext').hide();
        }
    });
    $('#edt_url').change(function(){
        if ($('#edt_url').val().indexOf('penpa-edit') > -1){
            $('#div_ext').show();
        } else {
            $('#div_ext').hide();
        }
    });
	// 保存された条件があれば読みだす
	var cond = localStorage.getItem('condyajilin');
	var condObj = JSON.parse(cond);
	if (condObj != null) {
		if (condObj.type === undefined) {
			$('#sel_type').val('nibunnogo');
		} else {
			$('#sel_type').val(condObj.type);
		}
	} else {
		// 初期条件。
		$('#sel_type').val('nibunnogo');
	}
    $('#loading').hide();
    $('#div_ext').hide();
});
