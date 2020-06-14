//$(function() {
//    $('#btn_submit').on('click', function(event){
//        if ($('#caption').text() == '処理中です。少々お待ちください……'){
//            // TODO 強引か…？
//            return;
//        }
//        $('#div_result').text('');
//        $('#caption').text('処理中です。少々お待ちください……');
//        var param = {};
//        param.div = 0;
//        $.ajax({
//            url: location.host.indexOf('localhost') == -1 ? 'https://myamyaapi.herokuapp.com/AkariBattle' : 'AkariBattle',
//            type: 'POST',
//            dataType: 'text',
//            data: param
//        }).done(function(result) {
//            var resultObj = JSON.parse(result);
//            $('#caption').text(resultObj.status);
//            $('#div_result').html(resultObj.result);
//        }).fail(function() {
//            $('#caption').text('通信時にエラーが発生しました');
//        });
//    });
//
//    // 追加要素に対してもイベントが有効にしたい場合は以下のように書くといい
//    $(document).on('click', 'rect', function(event){
//        if ($('#caption').text() == '処理中です。少々お待ちください……'){
//            // TODO 強引か…？
//            return;
//        }
//        $('#div_result').text('');
//        $('#caption').text('処理中です。少々お待ちください……');
//        var param = {};
//        param.div = 1;
//        param.x = 1;
//        param.y = 1;
//        $.ajax({
//            url: location.host.indexOf('localhost') == -1 ? 'https://myamyaapi.herokuapp.com/AkariBattle' : 'AkariBattle',
//            type: 'POST',
//            dataType: 'text',
//            data: param
//        }).done(function(result) {
//            var resultObj = JSON.parse(result);
//            $('#caption').text(resultObj.status);
//            $('#div_result').html(resultObj.result);
//        }).fail(function() {
//            $('#caption').text('通信時にエラーが発生しました');
//        });
//    });
//
//});
