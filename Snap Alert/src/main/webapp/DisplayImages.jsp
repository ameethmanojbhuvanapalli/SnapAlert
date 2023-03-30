<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Snap Alert</title>
<style>
table {
    margin: auto;
}
td {
    padding: 10px;
    text-align: center;
}
</style>
<script src="https://ajax.googleapis.com/ajax/libs/jquery/3.6.0/jquery.min.js"></script>
<script>
$(document).ready(function() {
    function getImages() {
        $.ajax({
            url: "ImageListRetrieverServlet",
            type: "GET",
            dataType: "json",
            success: function(imageList) {
                var imageTable = $("#imageTable");
                imageTable.empty(); // Clear the previous images
                $.each(imageList, function(index, imageName) {
                    var img = $("<img>");
                    img.attr("src", "images/" + imageName);
                    img.css("width", "100%");
                    img.css("height", "100%");
                    var row = $("<tr>").append($("<td>").append(img));
                    imageTable.append(row);
                });
            },
            error: function(jqXHR, textStatus, errorThrown) {
                console.log("Error retrieving image list: " + textStatus);
            }
        });
    }

    // Call the function once on document ready
    getImages();

    // Call the function every 5 seconds
    setInterval(function() {
        getImages();
    }, 5000);
});

</script>
</head>
<body style="background-color:black;">
<h2 align = center style="color:cyan;">Images</h2>
<table id="imageTable"></table>
</body>
</html>
