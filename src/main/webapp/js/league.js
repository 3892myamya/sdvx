$(function() {
	let text = '';
	let escape = function(string) {
		if (typeof string !== 'string') {
			return string;
		}
		return string.replace(/[&'`"<>]/g, function(match) {
			return {
				'&': '&amp;',
				"'": '&#x27;',
				'`': '&#x60;',
				'"': '&quot;',
				'<': '&lt;',
				'>': '&gt;',
			}[match]
		});
	}
	let showhide = function() {
		let ninzu = parseInt($('#sel_div').val());
		$(".member").each(function() {
			let value = parseInt($(this).attr('id').replaceAll('edt_', ''));
			if (value > ninzu) {
				$(this).hide();
			} else {
				$(this).show();
			}
		});
		$('#btn_copy').hide();
	}

	$('#btn_copy').on('click', function(event) {
		var textArea = document.createElement("textarea");
		textArea.value = text;
		textArea.style.position = "fixed";
		textArea.style.top = "0";
		textArea.style.left = "0";
		textArea.style.opacity = "0";
		document.body.appendChild(textArea);
		textArea.select();
		document.execCommand('copy');
		document.body.removeChild(textArea);
	});

	$('#sel_div').on('change', function() {
		showhide();
	});
	$('#btn_clear').on('click', function(event) {
		if (window.confirm('入力された名前をクリアします。よろしいですか？')) {
			$(".member").each(function() {
				$(this).val('');
			});
		}
	});
	$('#btn_start').on('click', function() {
		$('#div_result').text('');
		$('#btn_copy').show();
		let ninzu = parseInt($('#sel_div').val());
		let isShuffle = $('#cb_shuffle').prop('checked');
		let names = [];
		$(".member").each(function() {
			names.push($(this).val());
		});
		// 条件をローカルストレージ保存
		var cond = localStorage.getItem('condleague');
		var condObj = JSON.parse(cond);
		if (condObj != null) {
			condObj.ninzu = ninzu;
			condObj.names = names;
			condObj.isShuffle = isShuffle;
		} else {
			condObj = {
				ninzu: ninzu,
				names: names,
				isShuffle: isShuffle,
			};
		}
		cond = JSON.stringify(condObj);
		localStorage.setItem('condleague', cond);
		let league = [];
		for (let i = 0; i < ninzu; i++) {
			league.push([]);
		}
		let battleCnt = ninzu + ninzu % 2 - 1;
		for (let i = 0; i < battleCnt; i++) {
			for (let j = 0; j < battleCnt; j++) {
				for (let k = 0; k < battleCnt; k++) {
					var enemy = battleCnt - i - j - k - 1;
					if (enemy < 0) {
						enemy = enemy + battleCnt;
					}
					if (j == enemy) {
						if (ninzu % 2 == 1) {
							league[j].push(-1);
							break;
						} else {
							enemy = battleCnt;
						}
					}
					if (league[j].includes(enemy)) {
						break;
					}
					if (league[enemy].includes(j)) {
						break;
					}
					league[j].push(enemy);
					league[enemy].push(j);
					break;
				}
			}
		}
		let getName = function(index) {
			let result = $('#edt_' + (index + 1)).val();
			if (result == '') {
				result = "(" + (index + 1) + "人目)"
			}
			return escape(result);
		}
		if (isShuffle) {
			let wkShuffle = [];
			for (let i = 0; i < battleCnt; i++) {
				wkShuffle.push(i);
			}
			let shuffled = []
			for (let i = 0; i < ninzu; i++) {
				shuffled.push([]);
			}
			wkShuffle.sort(() => Math.random() - 0.5);
			for (let i = 0; i < league.length; i++) {
				for (let j = 0; j < wkShuffle.length; j++) {
					shuffled[i].push(league[i][(wkShuffle[j])]);
				}
			}
			league = shuffled;
		}
		let html = '';
		text = '';
		for (let i = 0; i < battleCnt; i++) {
			html = html + "<strong>" + (i + 1) + '回戦</strong><br>';
			text = text + (i + 1) + '回戦\n'
			let already = [];
			let nukeban = '';
			let textnukeban = ''
			for (let j = 0; j < league.length; j++) {
				if (already.includes(j)) {
					continue;
				}
				let playerName = getName(j);
				if (league[j][i] == -1) {
					nukeban = "・(抜け番:" + playerName + ")<br>";
					textnukeban = "・(抜け番:" + playerName + ")\n";
				} else {
					let enemyName = getName(league[j][i]);
					html = html + "・" + playerName + " - " + enemyName + '<br>';
					text = text + "・" + playerName + " - " + enemyName + '\n';
					already.push(j);
					already.push(league[j][i]);
				}
			}
			html = html + nukeban + "<br>";
			text = text + textnukeban + "\n";
		}
		$('#div_result').html(html);
	});

	// 保存された条件があれば読みだす
	var cond = localStorage.getItem('condleague');
	var condObj = JSON.parse(cond);
	if (condObj != null) {
		if (condObj.ninzu === undefined) {
			$('#sel_div').val(6);
		} else {
			$('#sel_div').val(condObj.ninzu);
		}
		if (condObj.names === undefined) {
			//
		} else {
			$(".member").each(function() {
				$(this).val(condObj.names[parseInt($(this).attr('id').replaceAll('edt_', '')) - 1]);
			});
		}
		if (condObj.isShuffle === undefined) {
			//
		} else {
			$('#cb_shuffle').prop("checked", condObj.isShuffle);
		}

	} else {
		// 初期条件
		$('#sel_div').val(6);
	}
	showhide();
});
