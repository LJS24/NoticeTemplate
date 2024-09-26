<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="kopo.poly.dto.MailDTO" %>
<%@ page import="kopo.poly.util.CmmUtil" %>
<%@ page import="kopo.poly.dto.MailDTO" %>
<%
    // MailController 함수에서 model 객체에 저장된 값 불러오기
    List<MailDTO> rList = (List<MailDTO>) request.getAttribute("rList");
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>메일 리스트</title>
    <link rel="stylesheet" href="/css/table.css"/>
</head>
<body>
<hr/>
<br/>
<div class="divTable minimalistBlack">
    <div class="divTableHeading">
        <div class="divTableRow">
            <div class="divTableHead">순번</div>
            <div class="divTableHead">제목</div>
            <div class="divTableHead">내용</div>
            <div class="divTableHead">받는 사람</div>
            <div class="divTableHead">보내는 사람</div>
            <div class="divTableHead">발송 시각</div>
        </div>
    </div>
    <div class="divTableBody">
        <%
            for (MailDTO dto : rList) {
        %>
        <div class="divTableRow">
            <div class="divTableCell"><%=CmmUtil.nvl(dto.getSeq())%></div>
            <div class="divTableCell" onclick="doDetail('<%=CmmUtil.nvl(dto.getTitle())%>')">
                <%=CmmUtil.nvl(dto.getTitle())%>
            </div>
            <div class="divTableCell"><%=CmmUtil.nvl(dto.getContents())%></div>
            <div class="divTableCell"><%=CmmUtil.nvl(dto.getToMail())%></div>
            <div class="divTableCell"><%=CmmUtil.nvl(dto.getFromMail())%></div>
            <div class="divTableCell"><%=CmmUtil.nvl(dto.getSendDt())%></div>
        </div>
        <%
            }
        %>
    </div>
</div>
</body>
</html>
