function download_files(inputJson) {
    let json = JSON.parse(inputJson);

    document.cookie = json.authStorage + "; domain=.uzh.ch; path=/";

    function download_next(i) {
        if (i >= json.entities.length) {
            return;
        }

        download_file(json.entities[i]);

        //console.log(i + ". Downloaded: " + json.entities[i])

        setTimeout(function () {
            download_next(i + 1);
        }, 200);
    }

    download_next(0);
}

function download_file(url) {
    let a = document.createElement('a');
    a.download = url.substring(url.lastIndexOf('/') + 1);
    a.href = url;
    a.target = '_parent';

    (document.body || document.documentElement).appendChild(a);
    if (a.click) {
        a.click();
    } else {
        $(a).click(); // Backup using jquery
    }

    a.parentNode.removeChild(a);
}