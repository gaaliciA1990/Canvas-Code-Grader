
console.log("Chrome ext ready");


(function () {

    function  addBtn() {
        var btn = document.createElement("input");
        btn.value = "Submit Check";
        btn.id = "submit_compile_code";
        btn.type = "submit";

        const el = document.querySelector(".FPdoLc.lJ9FBc center");

        console.log(el);
        el.appendChild(btn);
    }

    function defineEvents () {
        document
            .getElementById("submit_compile_code")
            .addEventListener("click", function
                (event){
                upload_compile(event.target.value.split(" ")[1]);
                //needs to grab the file from the field
            });

    }

    function upload_compile(str) {
        console.log("connect to our server and upload assignment")
    }

    addBtn();
    defineEvents();

})();