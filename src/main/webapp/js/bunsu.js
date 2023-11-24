$(function() {
	let keisan = function (){
		$('#div_result').text('');
		let value = $('#edt_1').val();
		if (value == '' || isNaN(value) || value < 0 || value > 100){
			$('#lbl_error').show();
			return;
		} else {
			$('#lbl_error').hide();
		}
		// 条件をローカルストレージ保存
		var cond = localStorage.getItem('condbunsu');
		var condObj = JSON.parse(cond);
		if (condObj != null) {
			condObj.val = value;
		} else {
			condObj = {
				val: value,
			};
		}		
		cond = JSON.stringify(condObj);
		localStorage.setItem('condbunsu', cond);
		let minSabun = Number(value) + 0.00001;
		let cnt = 0;
		let seisu = Math.trunc(value);
		value = Math.round((value - seisu) * 100000000000) / 100000000000;
		let result = [];
		let sabun = [];
		for (let i = 1; i <= 9999; i++) {
			let minus = value;
			let plus = value;
			while (true){
				let wk = cnt / i;
				if (wk < value){
					minus = value - wk;
					cnt++;
				} else {
					plus = wk - value;
					if (minus < plus){
						cnt--;
						if (minus < minSabun) {
							result.push(String(cnt + seisu * i).padStart(6,' ') + " / " + String(i).padStart(4,' ') + " : " + (Math.round((cnt + seisu * i) / i * 1000000) / 1000000).toFixed(6));
							sabun.push(-minus);
							minSabun = minus;
						}
					} else {
						if (plus < minSabun) {
							result.push(String(cnt + seisu * i).padStart(6,' ') + " / " + String(i).padStart(4,' ') + " : " + (Math.round((cnt + seisu * i) / i * 1000000) / 1000000).toFixed(6));
							sabun.push(plus);
							minSabun = plus;
						}
					}
					break;
				}
			}
		}
		let html = '';
		text = '';
		// 表示条件
		// 1.候補の最後は必ず表示
		// 2.後の候補を見て記号が入れ替わっていたら表示(ただし最初の1回はスキップ)
		let skipped = false;
		for (let i = 0; i < result.length; i++) {
			if (i != result.length - 1){
				if (sabun[i] < 0 && sabun[i+1] < 0){
					continue;
				}
				if (sabun[i] > 0 && sabun[i+1] > 0){
					continue;
				}
				if (!skipped){
					skipped = true;
					continue;
				}
			}
			html = html + result[i];
			if (sabun[i] < 0){
				html = html + '(<span class="minus">-' + (Math.round(-sabun[i] * 1000000) / 1000000).toFixed(6) + '</span>)';
			} else if (sabun[i] > 0){
				html = html + '(<span class="plus">+' + (Math.round(sabun[i] * 1000000) / 1000000).toFixed(6) + '</span>)';
			} else {
				html = html + '(<span>±' + (Math.round(sabun[i] * 1000000) / 1000000).toFixed(6) + '</span>)';
			}
			html = html + "<br>";
		}
		$('#div_result').html(html);		
	}
	$('#edt_1').on('keyup', function() {
		keisan()
	});
	$('#edt_1').on('change', function() {
		keisan()
	});
	// 保存された条件があれば読みだす
	var cond = localStorage.getItem('condbunsu');
	var condObj = JSON.parse(cond);
	if (condObj != null) {
		if (condObj.val === undefined) {
			// $('#edt_1').val(1.234);
		} else {
			$('#edt_1').val(condObj.val);
		}
	} else {
		// 初期条件
		// $('#edt_1').val(1.234);
	}
	keisan();
});
