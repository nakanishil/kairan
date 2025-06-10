let fileIndex = 1;

function addFileInput(currentInput) {
  const area = document.getElementById("fileUploadArea");
  const allInputs = area.querySelectorAll('input[type="file"]');
  if (Array.from(allInputs).some(input => input.value === "")) return;

  const div = document.createElement("div");
  div.className = "file-upload-item mb-3 border p-3 rounded";

  div.innerHTML = `
    <label class="form-label">ファイル${fileIndex + 1}：</label>
    <input type="file" name="fileList[${fileIndex}].file" class="form-control mb-2" onchange="addFileInput(this)" />
    <input type="text" name="fileList[${fileIndex}].fileName" placeholder="表示名を入力" class="form-control mb-2" />
    <button type="button" class="btn btn-outline-secondary btn-sm" onclick="clearFileInput(this)">入力をクリア</button>
  `;

  area.appendChild(div);
  fileIndex++;
}

function clearFileInput(button) {
  const wrapper = button.closest(".file-upload-item");
  const fileInput = wrapper.querySelector('input[type="file"]');
  const textInput = wrapper.querySelector('input[type="text"]');
  fileInput.value = "";
  textInput.value = "";
}
