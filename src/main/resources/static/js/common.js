$(() => {

});

function isNullOrEmpty(val) {
    if (val === "") {
        return true;
    } else if (val === null) {
        return true;
    } else if (val === undefined) {
        return true;
    } else {
        return false;
    }
}