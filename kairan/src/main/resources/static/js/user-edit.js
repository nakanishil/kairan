document.addEventListener('DOMContentLoaded', function(){
	const updateModal = document.getElementById('updateModal');
	
	updateModal.addEventListener('show.bs.modal', function(){
		document.getElementById('modalEmail').textContent = document.getElementById('email').value;
		document.getElementById('modalUserId').textContent = document.getElementById('userId').value;
		document.getElementById('modalName').textContent = document.getElementById('name').value;
		document.getElementById('modalFurigana').textContent = document.getElementById('furigana').value;
		document.getElementById('modalPhoneNumber').textContent = document.getElementById('phoneNumber').value;
	});
	
});
