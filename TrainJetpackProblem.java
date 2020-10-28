/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.geniemd.geniemd;

/**
 * @author awarres
 */

import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonSyntaxException;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;


public class AssessmentHtmlConverter {

    Example example;
    Patient patient;
    String Comnts;
    String Comntsdate;
    String Endpointcolor;
    String endpoints = "";
    String endpointaddress = "";
    int encounterId = 0;
    int userId = 0;
    Pharmacy pharmacy = null;
    private String extraData = "";

    private void setPharmacy() {
        try{
            boolean pharmacySet=false;
            if(encounterId==0)
                return;
            DosespotResource dr = new DosespotResource();
            EncountersPersistence ep = new EncountersPersistence();
            Encounter e=ep.getEncounter(encounterId);
            Response response = dr.searchPharmacies("{\"userID\":\""+ActiveSessions.getuserID(userId)+"\", \"pharmacyID\":\""+e.getPharmacyID()+"\"}");
            if(response.getEntity()!=null){
                System.out.println(response.getEntity());
                JSONObject jo = new JSONObject(response.getEntity().toString());
                if(jo.has("Items")){
                    if(jo.getJSONArray("Items").length()>0){
                        jo = jo.getJSONArray("Items").getJSONObject(0);
                        setDoseSpotPharmacyAttributes(jo.toString());
                        pharmacySet=true;
                    }
                }else{
                    setExtraDataPharmacy(extraData);
                }
                //pharmacy.address

            }
            if(!pharmacySet){

            }
        }catch(Exception e){
            setExtraDataPharmacy(extraData);
        }
    }

    private void setExtraDataPharmacy(String extraData) {
        pharmacy = new Pharmacy();
        try {
            JSONObject jo = new JSONObject(extraData).getJSONObject("pharmacy");

            pharmacy.address = jo.getString("address");
            pharmacy.name = jo.getString("name");
            pharmacy.city = jo.getString("city");
            pharmacy.state = jo.getString("state");
            pharmacy.zip = jo.getString("zipcode");
            pharmacy.phone = jo.getString("phone");
            pharmacy.fax = jo.getString("fax");

        } catch (Exception e) {
            pharmacy.address = "";
            pharmacy.city = "";
            pharmacy.name = "";
        }
    }

    private void setDoseSpotPharmacyAttributes(String input) throws JSONException {
        JSONObject jo = new JSONObject(input);
        pharmacy = new Pharmacy();
        pharmacy.address = jo.getString("Address1").trim() + " " + jo.getString("Address2").trim();
        pharmacy.city = jo.getString("City").trim();
        pharmacy.fax = jo.getString("PrimaryFax").trim();
        pharmacy.name = jo.getString("StoreName").trim();
        pharmacy.phone = jo.getString("PrimaryPhone").trim();
        pharmacy.state = jo.getString("State").trim();
        pharmacy.zip = jo.getString("ZipCode").trim();
    }

    private void setPatient(String json) throws JSONException {
        setPharmacy();
        JSONObject jo = new JSONObject(json);
        patient = new Patient();
        patient.firstName = jo.getString("firstName");
        patient.lastName = jo.getString("lastName");
        if (jo.has("age")) {
            patient.age = jo.getString("age");
        } else {
            patient.age = "";
        }

        if (jo.has("address")) {
            patient.address = jo.getString("address");
        } else {
            patient.address = "";
        }
        if (jo.has("city")) {
            patient.city = jo.getString("city");
        } else {
            patient.city = "";
        }
        if (jo.has("dateOfBirth")) {
            patient.dob = jo.getString("dateOfBirth");
        } else {
            patient.dob = "";
        }
        if (jo.has("email")) {
            patient.email = jo.getString("email");
        } else {
            patient.email = "";
        }
        if (jo.has("gender")) {
            patient.gender = jo.getInt("gender");
        } else {
            patient.gender = 2;
        }
        if (jo.has("phone")) {
            patient.phone = jo.getString("phone");
        } else if (jo.has("cell")) {
            patient.phone = jo.getString("cell");
            ;
        } else {
            patient.phone = "";
        }
        if (jo.has("pregnant")) {
            patient.pregnant = jo.getInt("pregnant");
        } else {
            patient.pregnant = 2;
        }
        if (jo.has("profileImageUrl")) {
            patient.profileImageUrl = jo.getString("profileImageUrl");
        } else {
            patient.profileImageUrl = "";
        }
        if (jo.has("patientState")) {
            patient.state = jo.getString("patientState");
        } else {
            patient.state = "";
        }
        if (jo.has("zip")) {
            patient.zip = jo.getString("zip");
        } else if (jo.has("zipcode")) {
            patient.zip = jo.getString("zipcode");
        }
        //return jo.getString("firstName") + " " + jo.getString("lastName");
    }

    private String getAssessmentHtml(String input) {
        try {
            String template = " <tr> <td width=\"55%\"> xx-question </td> <td width=\"20%\"> <b>xx-answer</b> </td> </tr>";
            String unselected = "<span>xx-value</span>";
            String selected = "<b>xx-value</b>";

            String html = "";
            JSONObject inputJson = new JSONObject(input);
            for (int i = 0; i < inputJson.getJSONArray("questions").length(); i++) {
                JSONObject question = inputJson.getJSONArray("questions").getJSONObject(i);

                if (!question.has("questionType")) {
                    String answer = "";
                    for (int a = 0; a < question.getJSONObject("question").getJSONArray("selections").length(); a++) {
                        String answerValue = "";
                        try {
                            answerValue = question.getJSONObject("question").getJSONArray("selections").getJSONObject(a).getString("selectionText");
                        } catch (JSONException e) {
                        }
                        if (question.getJSONObject("question").getJSONArray("selections").getJSONObject(a).getBoolean("selected")) {
                            answer += selected.replace("xx-value", answerValue) + "<br>";
                        } else {
                            answer += unselected.replace("xx-value", answerValue) + "<br>";
                        }
                    }
                    html += template.replace("xx-question", question.getJSONObject("question").getString("question")).replace("xx-answer", answer);
                } else {
                    String answerValue = "";
                    try {
                        answerValue = question.getJSONObject("answer").getString("answerValue");
                    } catch (JSONException e) {
                    }
                    html += template.replace("xx-question", question.getString("question")).replace("xx-answer", answerValue);
                }
            }
            return html;
        } catch (JSONException e) {
            return "<b>invalid patient interview json. " + e.getMessage() + "</b>";
        }
    }

    public String setHtml(String jsoninput, String Comnts, String Comntsdate, int endpnt, String extraData, String assessmentList, String subjectiveNotes, String interview, int encounterId, int userId, String visitSummary) {

        JSONObject json;
        this.encounterId = encounterId;
        this.userId = userId;
        this.Comnts = Comnts.replace("\n", "<br>");
        this.Comntsdate = Comntsdate;
        this.extraData = extraData;
        try {
            if ("".equals(jsoninput)) {
                jsoninput = interview;
            }

            json = new JSONObject(jsoninput);
//            json = XML.toJSONObject(jsoninput);
            example = new Gson().fromJson(json.toString(), Example.class);
            if (assessmentList != null && !"".equals(assessmentList)) {
                example.assessmentName = assessmentList;
            }

            if (example.patient == null) {
                setPatient(extraData);
            } else {
                patient = new Gson().fromJson(example.patient, Patient.class);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        if (endpnt == -1) {
            Endpointcolor = "#1b8a37,#6dd66d";
            endpoints = "Patient was presented with general message.";
        } else if (endpnt == 0) {
            Endpointcolor = "#9e0d0d,#f97d7a";
            endpoints = "Patient was triaged to Emergency Room.";
        } else if (endpnt == 1) {
            Endpointcolor = "#1b8a37,#6dd66d";
            endpoints = "Patient was triaged to a Clinic.";
        } else if (endpnt == 2) {
            Endpointcolor = "#9e0d0d,#f97d7a";
            endpoints = "Patient was triaged to laboratory.";
        } else if (endpnt == 3) {
            Endpointcolor = "#e09710,#ffd06e";
            endpoints = "Patient was presented with general message.";
        } else if (endpnt == 4) {
            Endpointcolor = "#9e0d0d,#f97d7a";
            endpoints = "Patient was notified to call 911.";
        } else if (endpnt == 5) {
            Endpointcolor = "#1b8a37,#6dd66d";
            endpoints = "Patient was asked to schedule an office visit.";
        } else if (endpnt == 6) {
            Endpointcolor = "#9e0d0d,#f97d7a";
            endpoints = "Patient was instructed to call the Nurse hotline.";
        } else if (endpnt == 7) {
            Endpointcolor = "#9e0d0d,#f97d7a";
            endpoints = "Patient was triaged to an Urgent Care facility.";
        } else if (endpnt == 8) {
            Endpointcolor = "#2f98f9,#90CAF9";
            endpoints = "Patient successfully completed an assessment.";
        } else if (endpnt == 9) {
            Endpointcolor = "#2f98f9,#90CAF9";
            endpoints = "Patient has requested iVisit.";
        } else {
            Endpointcolor = "#808080,#C0C0C0";
        }

        if ((endpnt == 8 || endpnt == 9) && patient.address != null && !patient.address.equals("")) {

            endpointaddress = "<tr>\n"
                    + "     <td>\n"
                    + " <h3> Pharmacy : - " + patient.address.trim().replace("ENDPOINT ADDRESS : -", "").replace(", Website:", "") + "</h3>\n"
                    + "</td>\n"
                    + "</tr>";
        }

        String assessmenttable = "";


        if (example.assessments == null) {
            assessmenttable = getAssessmentHtml(interview);
        } else if (example.assessments.size() > 0) {

            for (Assessment assessment : example.assessments) {

                if (assessment.nodeProperty != null) {

                    NodeProperty nodeProperty = null;
                    try {
                        nodeProperty = new Gson().fromJson(assessment.nodeProperty, NodeProperty.class);
                    } catch (JsonSyntaxException e) {
                        e.printStackTrace();
                    }
                    if (nodeProperty != null) {

                        if (nodeProperty.questionType != null) {

                            if (nodeProperty.questionType.equals("2") || nodeProperty.questionType.equals("3") || nodeProperty.questionType.equals("4")) {
                                assessmenttable = assessmenttable + "<tr>\n"
                                        + "\n"
                                        + "                    <td width=\"55%\">\n"
                                        + assessment.nodeTitle + "\n"
                                        + "                    </td>\n"
                                        + "                    <td width=\"20%\" >\n"
                                        + "                       <b>" + nodeProperty.questionParams.get(0).answer.varValue + "</b>\n"
                                        + "                    </td>\n"
                                        + "                </tr>\n";
                            } else if (nodeProperty.questionType.equals("1") || nodeProperty.questionType.equals("5")) {

                                String choices = ""; //1
                                for (QuestionParam questionParam : nodeProperty.questionParams) {
                                    if (questionParam.answer.varValue.equals("true")) {
                                        choices = choices + ((choices.equals("") ? "" : "</br>")) + " <b>" + questionParam.question + "</b>\n";
                                    } else {
                                        choices = choices + ((choices.equals("") ? "" : "</br>")) + " <span>" + questionParam.question + "</span>\n";
                                    }
                                }

                                assessmenttable = assessmenttable + "<tr>\n"
                                        + "\n"
                                        + "                    <td width=\"55%\">\n"
                                        + assessment.nodeTitle + "\n"
                                        + "                    </td>\n"
                                        + "                    <td width=\"20%\" >\n" + choices
                                        + "                    </td>\n"
                                        + "                </tr>\n";
                            } else if (nodeProperty.questionType.equals("0")) {
                                if ("true".equalsIgnoreCase(nodeProperty.askForReason) || "true".equalsIgnoreCase(nodeProperty.askForChoice)) {
                                    String choices = "";  //2
                                    for (QuestionParam questionParam : nodeProperty.questionParams) {
                                        assessmenttable = assessmenttable + "<tr>\n"
                                                + "\n"
                                                + "                    <td width=\"55%\">\n"
                                                + questionParam.question + "\n"
                                                + "                    </td>\n"
                                                + "                    <td width=\"20%\" >\n" + (questionParam.answer.varValue.equals("true") ? questionParam.answer.comment : "No")
                                                + "                    </td>\n"
                                                + "                </tr>\n";

                                        //                                        if (questionParam.answer.varValue.equals("true"))
                                        //                                            choices = choices +((choices.equals("")?"":"</br>"))+ " <b>" + questionParam.question + "</b>\n";
                                        //                                        else
                                        //                                            choices = choices +((choices.equals("")?"":"</br>"))+ " <span>" +questionParam.question + "</span>\n";
                                    }
                                } else {
                                    String choices = "";   //3
                                    for (QuestionParam questionParam : nodeProperty.questionParams) {
                                        if (questionParam.answer.varValue.equals("true")) {
                                            choices = choices + ((choices.equals("") ? "" : "</br>")) + " <b>" + questionParam.question + "</b>\n";
                                        } else {
                                            choices = choices + ((choices.equals("") ? "" : "</br>")) + " <span>" + questionParam.question + "</span>\n";
                                        }
                                    }

                                    assessmenttable = assessmenttable + "<tr>\n"
                                            + "\n"
                                            + "                    <td width=\"55%\">\n"
                                            + assessment.nodeTitle + "\n"
                                            + "                    </td>\n"
                                            + "                    <td width=\"20%\" >\n" + choices
                                            + "                    </td>\n"
                                            + "                </tr>\n";
                                }
                            }
                        }

                        if (nodeProperty.actionType != null) {

                            if (nodeProperty.actionType.equals("0")) {

                                String choices = "";

                                try {
                                    JSONArray jsonArray = new JSONArray(nodeProperty.actionParams.get(0).answer.varValue);

                                    for (int i = 0; i < jsonArray.length(); i++) {

                                        JSONObject jsonObject = jsonArray.getJSONObject(i);

                                        choices = choices + ((choices.equals("") ? "" : "</br>")) + "<a href=\"" + ((jsonObject.has("photoUrl")) ? jsonObject.getString("photoUrl") : "#") + "\" target=\"_blank\"><img src=\"" + ((jsonObject.has("photoThumbnailUrl")) ? jsonObject.getString("photoThumbnailUrl") : "#") + "\" alt=\"file\" style=\"width:200px;height:150px;\"></a>";
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                                assessmenttable = assessmenttable + "<tr>\n"
                                        + "\n"
                                        + "                    <td width=\"55%\">\n"
                                        + assessment.nodeTitle + "\n"
                                        + "                    </td>\n"
                                        + "                    <td width=\"20%\" >\n" + choices
                                        + "                    </td>\n"
                                        + "                </tr>\n";
                            }

                            if (nodeProperty.actionType.equals("1")) {
                                String choices = "";
                                try {
                                    JSONArray jsonArray = new JSONArray(nodeProperty.actionParams.get(0).answer.varValue);

                                    for (int i = 0; i < jsonArray.length(); i++) {

                                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                                        choices += ((choices.equals("") ? "" : "</br>")) + " <audio controls> <source src=\"" + ((jsonObject.has("url")) ? jsonObject.getString("url") : "#") + "\" type=\"audio/mpeg\"> Your browser does not support the audio element. </audio>";
                                        //choices = choices+((choices.equals("")?"":"</br>"))+ "<a href=\"" + ((jsonObject.has("url"))?jsonObject.getString("url"):"#") + "\" target=\"_blank\"><img src=\"" + Configuration.getBaseResourceURL() +"oem/play.png" + "\" alt=\"file\" style=\"width:64px;height:64px;\"></a>";
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                                assessmenttable = assessmenttable + "<tr>\n"
                                        + "\n"
                                        + "                    <td width=\"55%\">\n"
                                        + assessment.nodeTitle + "\n"
                                        + "                    </td>\n"
                                        + "                    <td width=\"20%\" >\n" + choices
                                        + "                    </td>\n"
                                        + "                </tr>\n";
                            }

                            if (nodeProperty.actionType.equals("2")) {

                                String choices = "";

                                try {
                                    JSONArray jsonArray = new JSONArray(nodeProperty.actionParams.get(0).answer.varValue);

                                    for (int i = 0; i < jsonArray.length(); i++) {

                                        JSONObject jsonObject = jsonArray.getJSONObject(i);

                                        choices = choices + ((choices.equals("") ? "" : "</br>")) + "<a href=\"" + ((jsonObject.has("videoUrl")) ? jsonObject.getString("videoUrl") : "#") + "\" target=\"_blank\"><img src=\"" + ((jsonObject.has("videoThumbnailUrl")) ? jsonObject.getString("videoThumbnailUrl") : "#") + "\" alt=\"file\" style=\"width:200px;height:150px;\"></a>";
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                                assessmenttable = assessmenttable + "<tr>\n"
                                        + "\n"
                                        + "                    <td width=\"55%\">\n"
                                        + ((nodeProperty.actionParams.get(0).actionTitle != null && !nodeProperty.actionParams.get(0).actionTitle.equals("")) ? nodeProperty.actionParams.get(0).actionTitle : assessment.nodeTitle) + "\n"
                                        + "                    </td>\n"
                                        + "                    <td width=\"20%\" >\n" + choices
                                        + "                    </td>\n"
                                        + "                </tr>\n";
                            }
                            if (nodeProperty.actionType.equals("3")) {

                                String choices = "";

                                try {

                                    pharmacy = new Gson().fromJson(nodeProperty.actionParams.get(0).answer.varValue, Pharmacy.class);

                                } catch (Exception e) {
                                    pharmacy = new Pharmacy();
                                    e.printStackTrace();
                                }
                            }

                            if (nodeProperty.actionType.equals("9")) {
                                assessmenttable = assessmenttable + "<td align=\"left\" colspan=\"2\"><h3 style=\"background:linear-gradient(to right,#ff2e05,#ff7054);padding: 10px; margin-bottom: 10px;\"><font color=\"#fff\">Medical Records</font></h3></td>";
                                //                                        "<tr>" +
                                //                        "                <td align=\"left\" colspan=\"2\">\n" +
                                //                        "                    <h3 style=\"background:linear-gradient(to right,#ff2e05,#ff7054);padding: 10px; margin-bottom: 10px;\"><font color=\"#fff\">Medical Records</font></h3>\n" +
                                //                        "                   \n" +
                                //                        "                </td>\n" +
                                //                        "            </tr>\n";

                                //                                assessmenttable=  assessmenttable+              "        <table style=\"width:100%; border-collapse: collapse;\">\n" +
                                //                        "            <tr>\n" +
                                //                        "                <td align=\"left\">\n" +
                                //                        "                    <h3 style=\"background:linear-gradient(to right,#ff89ae,#f44e82);padding: 10px; margin-bottom: 10px;\"><font color=\"#fff\">Medical Records</font></h3>\n" +
                                //                        "                   \n" +
                                //                        "                </td>\n" +
                                //                        "                \n" +
                                //                        "            </tr>\n" +
                                //                        "        </table>\n";
                                //String choices = "";
                                try {
                                    JSONArray jsonArray = new JSONArray(nodeProperty.actionParams.get(0).answer.varValue);

                                    for (int i = 0; i < jsonArray.length(); i++) {

                                        JSONObject jsonObject = jsonArray.getJSONObject(i);

                                        //---------------------------------
                                        assessmenttable = assessmenttable + "<tr>\n"
                                                + "\n"
                                                + "                    <td width=\"55%\">\n"
                                                + jsonObject.getString("description") + "\n"
                                                + "                    </td>\n"
                                                + "                    <td width=\"20%\" >\n" + "<a href=\"" + ((jsonObject.has("url")) ? jsonObject.getString("url") : "#") + "\" target=\"_blank\">" + jsonObject.getString("name") + "</a> <br>"
                                                + "                    </td>\n"
                                                + "                </tr>\n";

                                        //----------------------------------
                                        //choices = choices+((choices.equals("")?"":"</br>"))+ "<a href=\"" + ((jsonObject.has("url"))?jsonObject.getString("url"):"#") + "\" target=\"_blank\">"+jsonObject.getString("name")+"</a> <br>" + jsonObject.getString("description");
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                                //                                assessmenttable = assessmenttable + "<tr>\n" +
                                //                                        "\n" +
                                //                                        "                    <td width=\"55%\">\n" +
                                //                                        assessment.nodeTitle + "\n" +
                                //                                        "                    </td>\n" +
                                //                                        "                    <td width=\"20%\" >\n" + choices +
                                //                                        "                    </td>\n" +
                                //                                        "                </tr>\n";
                            }
                        }

                    }
                }
            }

        }
        String gender = "NA";
        String nationality = ExtraData.build(extraData).getNationality();
        String nirc = ExtraData.build(extraData).getNirc();
        if (patient.gender == null) {
            gender = "N/A";
        } else if (patient.gender == 0) {
            gender = "Male";
        } else if (patient.gender == 1) {
            gender = "Female";
        } else if (patient.gender == 2) {
            gender = "Other";
        }

        String Htmldata = " <!DOCTYPE html>\n"
                + "<html>\n"
                + "<head>\n"
                + " <script type=\"text/javascript\"> function getLocalDate(date){ var myDate = new Date(date); document.write(myDate.toLocaleString()); } </script>"
                + " <meta charset=\"UTF-8\">\n"
                + "    <title>My Assessment</title>\n"
                + "    <style>\n"
                + "        /* latin-ext */\n"
                + "            @font-face {\n"
                + "                font-family: 'Lato';\n"
                + "                font-style: normal;\n"
                + "                font-weight: 400;\n"
                + "                src: local('Lato Regular'), local('Lato-Regular'), url(https://fonts.gstatic.com/s/lato/v13/UyBMtLsHKBKXelqf4x7VRQ.woff2) format('woff2');\n"
                + "                unicode-range: U+0100-024F, U+1E00-1EFF, U+20A0-20AB, U+20AD-20CF, U+2C60-2C7F, U+A720-A7FF;\n"
                + "            }\n"
                + "        /* latin */\n"
                + "            @font-face {\n"
                + "                font-family: 'Lato';\n"
                + "                font-style: normal;\n"
                + "                font-weight: 400;\n"
                + "                src: local('Lato Regular'), local('Lato-Regular'), url(https://fonts.gstatic.com/s/lato/v13/1YwB1sO8YE1Lyjf12WNiUA.woff2) format('woff2');\n"
                + "                unicode-range: U+0000-00FF, U+0131, U+0152-0153, U+02C6, U+02DA, U+02DC, U+2000-206F, U+2074, U+20AC, U+2212, U+2215;\n"
                + "            }\n"
                + "td{\n"
                + "\t\t\tborder-width : 0px;\n"
                + "\t\t\tborder-color : black;\n"
                + "\t\t\t}span{\n"
                + "\t\t\tcolor : #c4c4c4;\n"
                + "\t\t\tfont-weight : normal;\n"
                + "\t\t\t}.mytable td {\n"
                + "     border-style: solid;   \n"
                + "    } \n"
                + "\n"
                + "\n"
                + ".mytable tr:nth-child(even) {background: #fff;}\n"
                + ".mytable tr:nth-child(odd) {background: #F5F5F5;}\n"
                + ".mytable1 tr:nth-child(even) { background:#fff;}\n"
                + ".mytable1 tr:nth-child(odd) {background:#F5F5F5;}\n"
                + ".mytable2 tr:nth-child(even) { background:#fff;}\n"
                + ".mytable2 tr:nth-child(odd) {background:#F5F5F5;}\n"
                + ".mytable3 tr:nth-child(even) { background:#fff;}\n"
                + ".mytable3 tr:nth-child(odd) {background:#F5F5F5;}\n"
                + ".img-circle {border-radius: 5px;max-width: 150px; max-height: 150px;}\n"
                + "              </style></head>\n"
                + "<body style=\"font-family:Lato, arial, Avenir;margin-left: 6%; margin-right: 6%; margin-bottom: 6%; margin-top: 2%;font-size: 18px;\">\n"
                + "    <div>\n"
                + "        <table style=\"border-collapse: collapse;width:100%;\">\n"
                + "            <tr>"
                + "                <td align=\"center\" ><img class=\"img-circle\" bgcolor=\"#FFFFF\" max-width=\"150px\" max-height=\"150px\" src=\"" + ((patient.profileImageUrl.equals("")) ? "https://s3-us-west-2.amazonaws.com/geniemd-images/avatar.jpg" : patient.profileImageUrl) + "\"/> </td>"
                + "            </tr>\n"
                + "            <tr>\n"
                + "                <td align=\"center\">\n"
                + "                    <h3 style=\"margin-bottom: 0;\">\n"
                + "                        <font color=\"#000\">\n"
                + "                         Assessment Report - " + ((example.assessmentName == null) ? "Patient Interview" : example.assessmentName)
                + "                        </font>\n"
                + "                    </h2>\n"
                + "\n"
                + "                </td>\n"
                + "            </tr>\n"
                + //                "             <tr>\n" +
                //                "                <td align=\"center\">\n" +
                //                "                    <h3 style=\"margin-top: 24px;margin-bottom: 0;\">\n" +
                //                "                        <font color=\"#000\" style=\"font-weight: bold\">\n" +
                //                "    <script language=\"JavaScript\"> getLocalDate("+DateTimeModule.getTimeStamp(example.assessmentStarted.replace("GMT", "").trim() + " UTC")+"); </script>\n" +
                //                "                        </font>\n" +
                //                "                    </h3>\n" +
                //                "                </td>\n" +
                //                "            </tr>\n" +
                "             <tr>\n"
                + "                    <td align=\"center\">\n"
                + "                        <h3 style=\"    margin-top: 24px; margin-bottom: 0;\"><font color=\"#000\" style=\"font-weight: bold\"> For " + patient.firstName + " " + patient.lastName + "</font></h3>\n"
                + "                    </td>\n"
                + "                </tr>\n"
                + "            \n"
                + "                  \n"
                + "    \n"
                + "         \n"
                + "         \n"
                + "<tr>\n"
                + "     <td>\n"
                + "     <h3 style=\"background: linear-gradient(to right," + Endpointcolor + "); padding: 10px;margin-bottom: 0px;\">\n"
                + "  <font color=\"#fff\">" + endpoints + "</font></h3>\n"
                + "</td>\n"
                + "</tr>        </table>\n"
                + "\n"
                + "\n"
                + "\n"
                + "   <table style=\"border-collapse: collapse;\">\n"
                + "            <tr>\n"
                + "                <td>\n"
                + "                    <br />\n"
                + "                </td>\n"
                + "            </tr>\n"
                + "        </table>\n"
                + "        <table style=\"width:100%; border-collapse: collapse;\">\n"
                + "            <tr>\n"
                + "                <td align=\"left\">\n"
                + "                    <h3 style=\"background:linear-gradient(to right,#1A237E,#7986CB);padding: 10px; margin-bottom: 10px;\"><font color=\"#fff\">Patient Info</font></h3>\n"
                + "                   \n"
                + "                </td>\n"
                + "                \n"
                + "            </tr>\n"
                + "        </table>\n"
                + "\n"
                + "            <table class=\"mytable\" style=\"border-collapse: collapse; width:100%;margin: auto;\"  cellpadding=\"8\">\n"
                + "            <tr bgcolor=\"#26A69A\">\n"
                + "                <td>Name\n"
                + "                </td>\n"
                + "\n"
                + "                <td width=\"55%\">\n"
                + "                   " + patient.firstName + " " + patient.lastName + "\n"
                + "                </td>\n"
                + "            </tr>\n"
                + "\n"
                + "            <tr>\n"
                + "                <td>Address\n"
                + "                </td>\n"
                + "\n"
                + "                <td width=\"55%\">\n"
                + patient.address + " " + patient.city + ", " + patient.state + " " + patient.zip + "\n"
                + "                </td>\n"
                + "            </tr>\n"
                + "\n"
                + "            <tr>\n"
                + "                <td>Phone Number\n"
                + "                </td>\n"
                + "\n"
                + "                <td width=\"55%\">\n"
                + patient.phone + "\n"
                + "                </td>\n"
                + "            </tr>\n"
                + "\n"
                + "            <tr>\n"
                + "                <td>Email\n"
                + "                </td>\n"
                + "\n"
                + "                <td width=\"55%\">\n"
                + patient.email + "\n"
                + "                </td>\n"
                + "            </tr>\n"
                + "\n"
                + "            <tr>\n"
                + "                <td >Date of Birth\n"
                + "                </td>\n"
                + "\n"
                + "                <td width=\"55%\">\n"
                + patient.dob + "\n"
                + "                </td>\n"
                + "            </tr>\n"
                + "\n"
                + "            <tr>\n"
                + "                <td >Gender\n"
                + "                </td>\n"
                + "\n"
                + "                <td width=\"55%\">\n"
                + gender + "\n"
                + "                </td>\n"
                + "            </tr>\n";
        if (!"".equals(nationality)) {
            Htmldata += "\n"
                    + "            <tr>\n"
                    + "                <td >Nationality\n"
                    + "                </td>\n"
                    + "\n"
                    + "                <td width=\"55%\">\n"
                    + nationality + "\n"
                    + "                </td>\n"
                    + "            </tr>\n";
        }

        if (!"".equals(nirc)) {
            Htmldata += "\n"
                    + "            <tr>\n"
                    + "                <td >NRIC\n"
                    + "                </td>\n"
                    + "\n"
                    + "                <td width=\"55%\">\n"
                    + nirc + "\n"
                    + "                </td>\n"
                    + "            </tr>\n";
        }

        Htmldata += "        </table>\n"
                + "        \n"
                + "\n"
                + "\n"
                + ((pharmacy == null) ? "" : "<table style=\"width:100%; border-collapse: collapse;\">\n"
                + "            <tr>\n"
                + "                <td align=\"left\">\n"
                + "                    <h3 style=\"background:linear-gradient(to right,#2E7D32,#66BB6A);padding: 10px; margin-bottom: 10px;\"><font color=\"#fff\">Pharmacy</font></h3>\n"
                + "                   \n"
                + "                </td>\n"
                + "                \n"
                + "            </tr>\n"
                + "        </table>\n"
                + "\n"
                + "            <table class=\"mytable\" style=\"border-collapse: collapse; width:100%;margin: auto;\"  cellpadding=\"8\">\n"
                + "            <tr bgcolor=\"#26A69A\">\n"
                + "                <td>Name\n"
                + "                </td>\n"
                + "\n"
                + "                <td width=\"55%\">\n"
                + pharmacy.name + "\n"
                + "                </td>\n"
                + "            </tr>\n"
                + "\n"
                + "            <tr>\n"
                + "                <td>Address\n"
                + "                </td>\n"
                + "\n"
                + "                <td width=\"55%\">\n"
                + pharmacy.address + " " + pharmacy.city + ", " + pharmacy.state + " " + pharmacy.zip + "\n"
                + "                </td>\n"
                + "            </tr>\n"
                + "\n"
                + "            <tr>\n"
                + "                <td>Phone Number\n"
                + "                </td>\n"
                + "\n"
                + "                <td width=\"55%\">\n"
                + pharmacy.phone + "\n"
                + "                </td>\n"
                + "            </tr>\n"
                + "\n"
                + "            <tr>\n"
                + "                <td>Fax\n"
                + "                </td>\n"
                + "\n"
                + "                <td width=\"55%\">\n"
                + pharmacy.fax + "\n"
                + "                </td>\n"
                + "            </tr>\n"
                + "\n"
                + "\n"
                + "        </table>\n")
                + "\n"
                + "\n"
                + //                "\t         <table style=\"width:100%; border-collapse: collapse;\">\n" +
                //                "            <tr>\n" +
                //                "                <td align=\"left\">\n" +
                //                "                    <h3 style=\"background:linear-gradient(to right,#2E7D32,#66BB6A); padding: 10px; margin-bottom: 10px;\"><font color=\"#fff\">Vitals</font></h3>\n" +
                //                "                </td>\n" +
                //                "             \n" +
                //                "            </tr>\n" +
                //                "        </table>\n" +
                //                "\n" +
                //                "        <table class=\"mytable1\" style=\"border-collapse: collapse; width:100%;margin: auto;\"  cellpadding=\"5\">\n" +
                //                "            <tr>\n" +
                //                "                <td width=\"55%\" style=\"background:linear-gradient(to right,#66BB6A,#C8E6C9);\">\n" +
                //                "                    <font color=\"#fff\" style=\"font-weight: bold; \">Name</font>\n" +
                //                "                </td>\n" +
                //                "                <td width=\"20%\" style=\"background:linear-gradient(to right,#66BB6A,#C8E6C9);\">\n" +
                //                "                    <font color=\"#fff\" style=\"font-weight: bold;\">Value</font>\n" +
                //                "                </td>\n" +
                //                "\t\t\t\t\n" +
                //                "            </tr>\n" +
                //                "                <tr>\n" +
                //                "                    <td width=\"55%\">\n" +
                //                "                        SPO2\n" +
                //                "                    </td>\n" +
                //                "                    <td width=\"20%\">\n" +
                //                "                     65/98 \n" +
                //                "                    </td>  \t\t\t   \n" +
                //                "                </tr>\n" +
                //                "\t\t\t\t <tr>\n" +
                //                "                    <td width=\"55%\">\n" +
                //                "                        BP (Systolic/Diastolic)\n" +
                //                "                    </td>\n" +
                //                "                    <td width=\"20%\">\n" +
                //                "                     128/81 \n" +
                //                "                    </td>  \t\t\t   \n" +
                //                "                </tr>\n" +
                //                "        </table>\n" +
                //                "             \n" +
                //                "<table style=\"border-collapse: collapse;\">\n" +
                //                "            <tr>\n" +
                //                "                <td>\n" +
                //                "                    <br />\n" +
                //                "                </td>\n" +
                //                "            </tr>\n" +
                //                "        </table>\n" +
                "  \n"
                + "                \n"
                + "      <table style=\"width:100%; border-collapse: collapse;\">\n"
                + "            <tr>\n"
                + "                <td align=\"left\">\n"
                + "                    <h3 style=\"background:linear-gradient(to right,#ec4545,#faa3a3);padding:10px;margin-bottom: 10px;\"><font color=\"#fff\">Allergies</font></h3>\n"
                + "                </td>\n"
                + "                \n"
                + "            </tr>\n"
                + "        </table>\n"
                + "\n" + ((true) ? "<table class=\"mytable3\" style=\"border-collapse: collapse; width:100%;margin: auto;\" cellpadding=\"5\">\n"
                + "                <tr>\n"
                + "                    <td width=\"55%\">\n"
                + ExtraData.build(extraData).getAllergies()
                + "                    </td>\n"
                        /*+
                "                    <td width=\"20%\">\n" +
                Comntsdate+
                "                    </td>    \n" */
                + "                </tr>\n"
                + "        </table>\n" : "")
                + "                \n"
                + "                \n"
                + "      <table style=\"width:100%; border-collapse: collapse;\">\n"
                + "            <tr>\n"
                + "                <td align=\"left\">\n"
                + "                    <h3 style=\"background:linear-gradient(to right,#2867f4,#aac2fa);padding:10px;margin-bottom: 10px;\"><font color=\"#fff\">Medications</font></h3>\n"
                + "                </td>\n"
                + "                \n"
                + "            </tr>\n"
                + "        </table>\n"
                + "\n" + ((true) ? "<table class=\"mytable3\" style=\"border-collapse: collapse; width:100%;margin: auto;\" cellpadding=\"5\">\n"
                + "                <tr>\n"
                + "                    <td width=\"55%\">\n"
                + ExtraData.build(extraData).getMedications()
                + "                    </td>\n"
                        /*+
                "                    <td width=\"20%\">\n" +
                Comntsdate+
                "                    </td>    \n" */
                + "                </tr>\n"
                + "        </table>\n" : "")
                + "                \n"
                + "                \n"
                + "      <table style=\"width:100%; border-collapse: collapse;\">\n"
                + "            <tr>\n"
                + "                <td align=\"left\">\n"
                + "                    <h3 style=\"background:linear-gradient(to right,#5e9c5b,#d3fad1);padding:10px;margin-bottom: 10px;\"><font color=\"#fff\">Conditions</font></h3>\n"
                + "                </td>\n"
                + "                \n"
                + "            </tr>\n"
                + "        </table>\n"
                + "\n" + ((true) ? "<table class=\"mytable3\" style=\"border-collapse: collapse; width:100%;margin: auto;\" cellpadding=\"5\">\n"
                + "                <tr>\n"
                + "                    <td width=\"55%\">\n"
                + ExtraData.build(extraData).getConditions()
                + "                    </td>\n"
                        /*+
                "                    <td width=\"20%\">\n" +
                Comntsdate+
                "                    </td>    \n" */
                + "                </tr>\n"
                + "        </table>\n" : "")
                + "                \n"
                + (ExtraData.build(extraData).getEyeExam().equals("") ? "" : "      <table style=\"width:100%; border-collapse: collapse;\">\n"
                + "            <tr>\n"
                + "                <td align=\"left\">\n"
                + "                    <h3 style=\"background:linear-gradient(to right,#5b779c,#a0c9ff);padding:10px;margin-bottom: 10px;\"><font color=\"#fff\">Vision Acuity</font></h3>\n"
                + "                </td>\n"
                + "                \n"
                + "            </tr>\n"
                + "        </table>\n"
                + "\n" + ((true) ? "<table class=\"mytable3\" style=\"border-collapse: collapse; width:100%;margin: auto;\" cellpadding=\"5\">\n"
                + "                <tr>\n"
                + "                    <td width=\"55%\">\n"
                + ExtraData.build(extraData).getEyeExam()
                + "                    </td>\n"
                        /*+
                "                    <td width=\"20%\">\n" +
                Comntsdate+
                "                    </td>    \n" */
                + "                </tr>\n"
                + "        </table>\n" : "")
                + "                \n")
                + "      <table style=\"width:100%; border-collapse: collapse;\">\n"
                + "            <tr>\n"
                + "                <td align=\"left\">\n"
                + "                    <h3 style=\"background:linear-gradient(to right,#1565C0,#2196F3);padding:10px;margin-bottom: 10px;\"><font color=\"#fff\">Assessment</font></h3>\n"
                + "                </td>\n"
                + "                \n"
                + "            </tr>\n"
                + "        </table>\n"
                + "\n"
                + "        <table class=\"mytable2\" style=\"border-collapse: collapse; width:100%;margin: auto;\" cellpadding=\"5\" >\n"
                + "            <tr>\n"
                + "                <td  width=\"55%\" style=\"background:linear-gradient(to right,#64B5F6,#BBDEFB);\">\n"
                + "                    <font color=\"#fff\" style=\"font-weight: bold\">Assessment Question</font>\n"
                + "                </td>\n"
                + "                <td  width=\"20%\" style=\"background:linear-gradient(to right,#64B5F6,#BBDEFB);\">\n"
                + "                    <font color=\"#fff\" style=\"font-weight: bold\">Patient Answer</font>\n"
                + "                </td>\n"
                + "            </tr>\n"
                + "             \n";
        String endcode = "\t\t     <table style=\"width:100%; border-collapse: collapse;\">\n"
                + "            <tr>\n"
                + "                <td align=\"left\">\n"
                + "                    <h3 style=\"background:linear-gradient(to right,#2999a6,#a5d3d9);padding:10px;margin-bottom: 10px;\"><font color=\"#fff\">Provider Entered Data</font></h3>\n"
                + "                </td>\n"
                + "               \n"
                + "            </tr>\n"
                + "        </table>\n" + ExtraData.build(extraData).getDataByProvider()
                // notes to patient


                + ((subjectiveNotes != null && !"".equals(subjectiveNotes)) ? "<table style=\"width:100%; border-collapse: collapse;\">\n"
                + "            <tr>\n"
                + "                <td align=\"left\">\n"
                + "                    <h3 style=\"background:linear-gradient(to right,#1e8edd,#93C0D7);padding:10px;margin-bottom: 10px;\"><font color=\"#fff\">Provider Private Notes</font></h3>\n"
                + "                </td>\n"
                + "               \n"
                + "            </tr>\n"
                + "        </table>\n" : "")
                + "\n" + ((subjectiveNotes != null && !"".equals(subjectiveNotes)) ? "<table class=\"mytable3\" style=\"border-collapse: collapse; width:100%;margin: auto;\" cellpadding=\"5\">\n"
                + "                <tr>\n"
                + "                    <td width=\"55%\">\n"
                + subjectiveNotes.replace("\n", "<br>")
                + "                    </td>\n"
                + "                </tr>\n"
                + "        </table>\n" : "")
                + "     <table style=\"border-collapse: collapse;\">\n"
                + "            <tr>\n"
                + "                <td>\n"
                + "                    <br />\n"
                + "                </td>\n"
                + "            </tr>\n"
                + "        </table>\n"
                + "        \n"
                + "\n"
                + "\n"
                + "</div></body>\n"
                + "</html>\n ";

        return Htmldata + assessmenttable + getVisitSummary(visitSummary, subjectiveNotes) + endcode;

    }

    public String getVisitSummary(String visitSummaryjson, String subjectiveNotes) {

        try {
            JSONArray visitsummaryArray = new JSONArray(visitSummaryjson);

            String visitSummary = "";

            for (int i = 0; i < visitsummaryArray.length(); i++) {
                visitSummary = visitSummary + geteachVisitSummary(visitsummaryArray.getJSONObject(i), subjectiveNotes);
            }
            return visitSummary;

        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }

    }

    private String getCodes(JSONObject jsonObject) throws JSONException {

        String codes = "";
        if (jsonObject.has("iCD10Codes")) {
            codes = codes + getCode(jsonObject.getString("iCD10Codes"), "ICD10");
        }
        if (jsonObject.has("cPTCodes")) {
            codes = codes + getCode(jsonObject.getString("cPTCodes"), "CPT Code");
            ;
        }
        if (jsonObject.has("snomedCodes")) {
            codes = codes + getCode(jsonObject.getString("snomedCodes"), "Snomed Code");
            ;
        }
        if (!codes.equals("")) {
            codes = "<table style=\"width:100%; border-collapse: collapse;\">\n" +
                    "                <tr>\n" +
                    "                    <td align=\"left\">\n" +
                    "                        <h5\n" +
                    "                            style=\"background:linear-gradient(to right,#64B5F6,#BBDEFB);padding:10px;margin-bottom: 10px;\">\n" +
                    "                            <font color=\"#fff\">Codes\n" +
                    "                            </font>\n" +
                    "                        </h5>\n" +
                    "                    </td>\n" +
                    "\n" +
                    "                </tr>\n" +
                    "            </table>\n" +
                    "\n" +
                    "            <table class=\"mytable\" style=\"border-collapse: collapse; width:100%;margin: auto;\" cellpadding=\"8\">" + codes + "</table>";
        }
        return codes;
    }

    private String getSickSlip(JSONObject jsonObject) throws JSONException {

        String slip = "", slipData = "";

        if (jsonObject.has("sickSlip") && !TextUtils.isEmpty(jsonObject.getString("sickSlip"))) {
            JSONObject ob = new JSONObject(jsonObject.getString("sickSlip"));
            if (ob.has("name") && ob.getString("name") != null && ob.getString("url") != null) {
                slipData = " <table class=\"mytable3\" style=\"border-collapse: collapse; width:100%;margin: auto;\" cellpadding=\"5\">\n" +
                        "                <td>" + ob.getString("name") + "\n" +
                        "                </td>\n" +
                        "\n" +
                        "                <td width=\"55%\">\n" +
                        "                    <a href=" + ob.getString("url") + "\n" +
                        "                        target=\"_blank\">Click to view document</a>\n" +
                        "                </td>\n" +
                        "                </tr>\n" +
                        "            </table>";

            }
        }

        if (!slipData.equals("")) {
            slip = "<table style=\"width:100%; border-collapse: collapse;\">\n" +
                    "                <tr>\n" +
                    "                    <td align=\"left\">\n" +
                    "                        <h5\n" +
                    "                            style=\"background:linear-gradient(to right,#64B5F6,#BBDEFB);padding:10px;margin-bottom: 10px;\">\n" +
                    "                            <font color=\"#fff\">Sick Slip\n" +
                    "\n" +
                    "                            </font>\n" +
                    "                        </h5>\n" +
                    "                    </td>\n" +
                    "\n" +
                    "                </tr>\n" +
                    "            </table>\n" + "\n" + slipData;
        }


        return slip;
    }


    private String getEducationMaterial(JSONObject jsonObject) throws JSONException {
        String educationMaterial = "", educationData = "";

        if (jsonObject.has("educationMaterial") && jsonObject.getString("educationMaterial") != null) {
            JSONArray array = new JSONArray(jsonObject.getString("educationMaterial"));

            if (array != null && array.length() != 0)
                for (int i = 0; i < array.length(); i++) {
                    if (!TextUtils.isEmpty(getEachEducationData(array.getJSONObject(i))))
                        educationData =
                                educationData + getEachEducationData(array.getJSONObject(i));
                }

        }

        if (!educationData.equals("")) {
            educationMaterial = "<table style=\"width:100%; border-collapse: collapse;\">\n" +
                    "                <tr>\n" +
                    "                    <td align=\"left\">\n" +
                    "                        <h5\n" +
                    "                            style=\"background:linear-gradient(to right,#64B5F6,#BBDEFB);padding:10px;margin-bottom: 10px;\">\n" +
                    "                            <font color=\"#fff\">Educational Material\n" +
                    "\n" +
                    "                            </font>\n" +
                    "                        </h5>\n" +
                    "                    </td>\n" +
                    "\n" +
                    "                </tr>\n\n" +
                    "            </table>" + "<table class=\"mytable3\" style=\"border-collapse: collapse; width:100%;margin: auto;\" cellpadding=\"5\">" + educationData + "</table>";
        }

        return educationMaterial;
    }

    private String getEachEducationData(JSONObject jsonObject) throws JSONException {

        JSONObject edu = jsonObject;
        String educationData = "";

        if (edu.has("title") && edu.has("url")) {
            educationData = "<tr>\n" +
                    "                    <td>" + edu.getString("title") + "\n" +
                    "                    </td>\n" +
                    "\n" +
                    "                    <td width=\"55%\">\n" +
                    "                        <a href=" + edu.getString("url") + "\n" +
                    "                            target=\"_blank\">Click to view document</a>\n" +
                    "                    </td>\n" +
                    "                </tr>";
        }


        return educationData;
    }

    private String getPrescription(JSONObject jsonObject) throws JSONException {
        String prescription = "", prescriptionData = "";

        if (jsonObject.has("ePrescription") && !TextUtils.isEmpty(jsonObject.getString("ePrescription"))) {
            JSONArray array = new JSONArray(jsonObject.getString("ePrescription"));

            if (array != null && array.length() != 0)
                for (int i = 0; i < array.length(); i++) {
                    if (!TextUtils.isEmpty(getEachPrescriptionData(array.getJSONObject(i))))
                        prescriptionData =
                                prescriptionData + getEachPrescriptionData(array.getJSONObject(i));
                }

        }

        if (!prescriptionData.equals("")) {
            prescription = "<table style=\"width:100%; border-collapse: collapse;\">\n" +
                    "                <tr>\n" +
                    "                    <td align=\"left\">\n" +
                    "                        <h5\n" +
                    "                            style=\"background:linear-gradient(to right,#64B5F6,#BBDEFB);padding:10px;margin-bottom: 10px;\">\n" +
                    "                            <font color=\"#fff\">Prescription\n" +
                    "\n" +
                    "                            </font>\n" +
                    "                        </h5>\n" +
                    "                    </td>\n" +
                    "\n" +
                    "                </tr>\n\n" +
                    "            </table>" + "<table class=\"mytable3\" style=\"border-collapse: collapse; width:100%;margin: auto;\" cellpadding=\"5\">" + prescriptionData + "</table>";
        }

        return prescription;
    }

    private String getEachPrescriptionData(JSONObject jsonObject) throws JSONException {

        JSONObject edu = jsonObject;
        String prescriptionData = "";

        if (edu.has("title") && edu.has("url")) {
            prescriptionData = "<tr>\n" +
                    "                    <td>" + edu.getString("title") + "\n" +
                    "                    </td>\n" +
                    "\n" +
                    "                    <td width=\"55%\">\n" +
                    "                        <a href=" + edu.getString("url") + "\n" +
                    "                            target=\"_blank\">Click to view document</a>\n" +
                    "                    </td>\n" +
                    "                </tr>";
        }


        return prescriptionData;
    }

    private String getPatientReferral(JSONObject jsonObject) throws JSONException {
        String patientReferral = "", referralData = "";

        if (jsonObject.has("patientReferral") && jsonObject.getString("patientReferral") != null) {
            JSONArray array = new JSONArray(jsonObject.getString("patientReferral"));

            if (array != null && array.length() != 0)
                for (int i = 0; i < array.length(); i++) {
                    if (!TextUtils.isEmpty(getPatientReferralData(array.getJSONObject(i))))
                        referralData =
                                referralData + getPatientReferralData(array.getJSONObject(i));
                }

        }

        if (!referralData.equals("")) {
            patientReferral = "<table style=\"width:100%; border-collapse: collapse;\">\n" +
                    "                <tr>\n" +
                    "                    <td align=\"left\">\n" +
                    "                        <h5\n" +
                    "                            style=\"background:linear-gradient(to right,#64B5F6,#BBDEFB);padding:10px;margin-bottom: 10px;\">\n" +
                    "                            <font color=\"#fff\">Referral Letter\n" +
                    "\n" +
                    "                            </font>\n" +
                    "                        </h5>\n" +
                    "                    </td>\n" +
                    "\n" +
                    "                </tr>\n\n" +
                    "            </table>" + "<table class=\"mytable3\" style=\"border-collapse: collapse; width:100%;margin: auto;\" cellpadding=\"5\">" + referralData + "</table>";
        }

        return patientReferral;
    }

    private String getPatientReferralData(JSONObject jsonObject) throws JSONException {

        JSONObject referral = jsonObject;
        String referralData = "";

        if (referral.has("name") && referral.has("url")) {
            referralData = "<tr>\n" +
                    "                    <td>" + referral.getString("name") + "\n" +
                    "                    </td>\n" +
                    "\n" +
                    "                    <td width=\"55%\">\n" +
                    "                        <a href=" + referral.getString("url") + "\n" +
                    "                            target=\"_blank\">Click to view document</a>\n" +
                    "                    </td>\n" +
                    "                </tr>";
        }


        return referralData;
    }


    private String geteachVisitSummary(JSONObject jsonObject, String subjectiveNotes) {

        try {

            String Codes = getCodes(jsonObject), educationMaterial = getEducationMaterial(jsonObject);

            String sickSlip = getSickSlip(jsonObject);
            String patientReferral = getPatientReferral(jsonObject);

            String prescription = getPrescription(jsonObject);


            String visitSummary = "<table style=\"width:100%; border-collapse: collapse;\">\n" +
                    "                <tr>\n" +
                    "                    <td align=\"left\">\n" +
                    "                        <h3\n" +
                    "                            style=\"background:linear-gradient(to right,#1565c0,#2196F3);padding:10px;margin-bottom: 10px;\">\n" +
                    "                            <font color=\"#fff\">After Visit Summary Report by: " + ((jsonObject.has("providerFullName") ? jsonObject.getString("providerFullName") : "")) + "</font>\n" +
                    "                        </h3>\n" +
                    "                    </td>\n" +
                    "\n" +
                    "                </tr>\n" +
                    "            </table>\n" +
                    "\n" +
                    "            <table style=\"width:100%; border-collapse: collapse;\">\n" +
                    "                <tr>\n" +
                    "                    <td align=\"left\">\n" +
                    "                        <h5\n" +
                    "                            style=\"background:linear-gradient(to right,#64B5F6,#BBDEFB);padding:10px;margin-bottom: 10px;\">\n" +
                    "                            <font color=\"#fff\">Patient Note\n" +
                    "                            </font>\n" +
                    "                        </h5>\n" +
                    "                    </td>\n" +
                    "\n" +
                    "                </tr>\n" +
                    "            </table>\n" +
                    "\n" +
                    "            <table class=\"mytable3\" style=\"border-collapse: collapse; width:100%;margin: auto;\" cellpadding=\"5\">\n" +
                    "                <tr>\n" +
                    "                    <td width=\"55%\">\n" + ((jsonObject.has("patientNotes") ? jsonObject.getString("patientNotes") : "")) +
                    "                    </td>\n" +
                    "                </tr>\n" +
                    "            </table>\n" +
                    "\n" +
                    Codes +
                    "\n" + prescription + "\n" +
                    "\n" + educationMaterial + "\n" +
                    "\n" + sickSlip +
                    "\n" + patientReferral +
                    "           \n" +
                    ((subjectiveNotes != null && !"".equals(subjectiveNotes)) ? "<table style=\"width:100%; border-collapse: collapse;\">\n" +
                            "                <tr>\n" +
                            "                    <td align=\"left\">\n" +
                            "                        <h5\n" +
                            "                            style=\"background:linear-gradient(to right,#64B5F6,#BBDEFB);padding:10px;margin-bottom: 10px;\">\n" +
                            "                            <font color=\"#fff\">Provider Private Note\n" +
                            "\n" +
                            "                            </font>\n" +
                            "                        </h5>\n" +
                            "                    </td>\n" +
                            "\n" +
                            "                </tr>\n" +
                            "            </table>\n" : "")
                    + "\n" + ((subjectiveNotes != null && !"".equals(subjectiveNotes)) ? "<table class=\"mytable3\" style=\"border-collapse: collapse; width:100%;margin: auto;\" cellpadding=\"5\">\n"
                    + "                <tr>\n"
                    + "                    <td width=\"55%\">\n"
                    + subjectiveNotes.replace("\n", "<br>")
                    + "                    </td>\n"
                    + "                </tr>\n"
                    + "        </table>\n" : "") +
                    "                    </td>\n" +
                    "                </tr>\n" +
                    "            </table>\n" +
                    "\n" +
                    "\n" + ((jsonObject.has("electricSign") && !jsonObject.getString("electricSign").equals("")) ? "<table style=\"width:100%; border-collapse: collapse;\">\n" +
                    "                <tr>\n" +
                    "                    <td align=\"left\">\n" +
                    "                        <h5\n" +
                    "                            style=\"background:linear-gradient(to right,#64B5F6,#BBDEFB);padding:10px;margin-bottom: 10px;\">\n" +
                    "                            <font color=\"#fff\">Signature\n" +
                    "\n" +
                    "                            </font>\n" +
                    "                        </h5>\n" +
                    "                    </td>\n" +
                    "\n" +
                    "                </tr>\n" +
                    "            </table>\n" : "")
                    + "\n" + ((jsonObject.has("electricSign") && !jsonObject.getString("electricSign").equals("")) ? "<table class=\"mytable3\" style=\"border-collapse: collapse; width:100%;margin: auto;\" cellpadding=\"5\">\n"
                    + "                <tr>\n"
                    + "                    <td width=\"55%\">\n"
                    + jsonObject.getString("electricSign").replace("\n", "<br>")
                    + "                    </td>\n"
                    + "                </tr>\n"
                    + "        </table>\n" : "") +
                    "                    </td>\n" +
                    "                </tr>\n" +
                    "            </table>\n";


            return visitSummary;

        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    private String getCode(String codes, String title) {

        if (TextUtils.isEmpty(codes)) {
            return "";
        } else {
            return "<tr bgcolor=\"#26A69A\">\n" +
                    "                    <td>" + title + "\n" +
                    "                    </td>\n" +
                    "\n" +
                    "                    <td width=\"55%\">\n" +
                    codes +
                    "                        \n" +
                    "                    </td>\n" +
                    "                </tr>";
        }

    }

    public class Example {
        public List<String> symptoms = null;
        public String assessmentStarted = "";
        public String assessmentName = "Patient Interview";
        public String patient = "";
        public List<Assessment> assessments = null;
    }

    public class Assessment {

        public Integer languageID;
        public String assessmentInfo;
        public Integer nodeID;
        public String nodeTitle;
        public Integer nodeType;
        public Integer assessmentID;
        public String nodeProperty;
    }

    public class Patient {

        public String state;
        public String lastName;
        public Integer pregnant;
        public String firstName;
        public String zip;
        public String phone;
        public String dob;
        public String profileImageUrl;
        public String address;
        public String city;
        public String email;
        public String age;
        public Integer gender;
    }

    public class Pharmacy {

        private String name = "";
        private String address = "";
        private String city = "";
        private String state = "";
        private String zip = "";
        private String phone = "";
        private String fax = "";
    }

    public class NodeProperty {

        public String questionType;
        public String actionType;
        public List<QuestionParam> questionParams = null;
        public List<QuestionParam> actionParams = null;
        public String askForReason;
        public String askForChoice;
    }

    public class Answer {

        public String varName;
        public String varType;
        public String varValue;
        public String comment;
    }

    public class MedicalRecord {

        public String name;
        public String description;
        public String url;
    }

    public class PhotoValue {

        public String photoUrl;
        public String photoThumbnailUrl;

    }

    public class QuestionParam {

        public String min;
        public Answer answer;
        public String type;
        public String max;
        public String question;
        public String actionTitle;

    }

    static class ExtraData {

        static JSONObject jo;

        public static ExtraData build(String data) {
            return new ExtraData(data);

        }

        private ExtraData(String data) {
            try {
                jo = new JSONObject(data);
            } catch (Exception e) {
            }
        }


        public String getAllergies() {
            try {
                String allergies = jo.getString("allergies");//"[{ \"type\": \"skin\", \"name\": \"poison ivy\" }, { \"type\": \"eyes\", \"name\": \"pollen\" } ]"; //jo.getString("allergies");
                try {
                    JSONArray a = new JSONArray(allergies);
                    String allergyTable = "";
                    if (a.length() > 0) {
                        allergyTable = "<table class=\"mytable2\" align=\"left\" style=\"border-collapse: collapse; width:100%;margin: auto;\" cellpadding=\"5\"><tr><td style=\"font-size:larger\">Type</td><td  style=\"font-size:larger\">Name</td></tr>";

                        for (int i = 0; i < a.length(); i++) {
                            if (a.getString(i).startsWith("{"))
                                allergyTable += "<tr><td>" + a.getJSONObject(i).getString("type") + "</td><td>" + a.getJSONObject(i).getString("name") + "</td></tr>";
                            else {
                                if (i == 0)
                                    allergyTable = "";
                                allergyTable += "<tr><td>" + a.getString(i) + "</td></tr>";
                            }
                        }
                        allergyTable += "</table>";
                    }
                    return allergyTable;
                } catch (Exception ex) {
                    return allergies;
                }
            } catch (Exception e) {
                return "Not specified";
            }
        }

        public String getEyeExam() {
            try {
                if (!jo.has("VisionTest")) {
                    return "";
                }
                String visionTest = jo.getString("VisionTest");//"[{ \"type\": \"skin\", \"name\": \"poison ivy\" }, { \"type\": \"eyes\", \"name\": \"pollen\" } ]"; //jo.getString("allergies");
                try {
                    JSONArray a = new JSONArray(visionTest);
                    String visionTestTable = "<table class=\"mytable2\" align=\"left\" style=\"border-collapse: collapse; width:100%;margin: auto;\" cellpadding=\"5\">";

                    for (int i = 0; i < a.length(); i++) {
                        visionTestTable += "<tr>";
                        if (a.getJSONObject(i).has("type")) {
                            if (a.getJSONObject(i).getString("type").equals("near")) {
                                visionTestTable += "<td>Near Test</td>";
                            } else {
                                visionTestTable += "<td>Distance Test</td>";
                            }
                            if (a.getJSONObject(i).has("right"))
                                visionTestTable += "<td>Right</td><td>" + a.getJSONObject(i).getString("right") + "</td>";
                            if (a.getJSONObject(i).has("left"))
                                visionTestTable += "<td>Left</td><td>" + a.getJSONObject(i).getString("left") + "</td>";
                            if (a.getJSONObject(i).has("both"))
                                visionTestTable += "<td>Both</td><td>" + a.getJSONObject(i).getString("both") + "</td>";
                            if (a.getJSONObject(i).has("date"))
                                visionTestTable += "<td>Date</td><td>" + a.getJSONObject(i).getString("date") + "</td>";
                            visionTestTable += "</tr>";
                        }
                    }
                    visionTestTable += "</table>";
                    return visionTestTable;
                } catch (Exception ex) {
                    return visionTest;
                }
            } catch (Exception e) {
                return "Not specified";
            }
        }

        public String getMedications() {
            try {
                String medications = jo.getString("medications");//"[{ \"name\": \"aspirin\", \"dosage\": \"85 mg\", \"frequency\": \"once a day\" }, { \"name\": \"warfafin\", \"dosage\": \"5 mg\", \"frequency\": \"once a day\" } ]"; //jo.getString("medications");
                try {
                    String allergyTable = "";
                    JSONArray a = new JSONArray(medications);
                    if (a.length() > 0) {
                        allergyTable = "<table class=\"mytable2\" align=\"left\" style=\"border-collapse: collapse; width:100%;margin: auto;\" cellpadding=\"5\"><tr><td  style=\"font-size:larger\">Name</td><td  style=\"font-size:larger\">Dosage</td><td  style=\"font-size:larger\">Frequency</td></tr>";

                        for (int i = 0; i < a.length(); i++) {
                            if (a.getString(i).startsWith("{"))
                                allergyTable += "<tr><td>" + a.getJSONObject(i).getString("name") + "</td><td>" + a.getJSONObject(i).getString("dosage") + "</td><td>" + a.getJSONObject(i).getString("frequency") + "</td></tr>";
                            else {
                                if (i == 0)
                                    allergyTable = "";
                                allergyTable += "<tr><td>" + a.getString(i) + "</td></tr>";
                            }
                        }
                        allergyTable += "</table>";
                    }
                    return allergyTable;
                } catch (Exception ex) {
                    return medications;
                }
                //return jo.getString("medications");
            } catch (Exception e) {
                return "Not specified";
            }
        }

        public String getConditions() {
            try {

                String conditions = jo.getString("conditions"); //"[{ \"name\": \"high blood pressure\" }, { \"name\": \"near sightedness\" } ]"; //jo.getString("conditions");
                try {
                    String conditionTable = "";
                    JSONArray a = new JSONArray(conditions);
                    if (a.length() > 0) {
                        conditionTable = "<table class=\"mytable2\" align=\"left\" style=\"border-collapse: collapse; width:100%;margin: auto;\" cellpadding=\"5\"><tr><td style=\"font-size:larger\">Name</td></tr>";

                        for (int i = 0; i < a.length(); i++) {
                            if (a.getString(i).startsWith("{"))
                                conditionTable += "<tr><td>" + a.getJSONObject(i).getString("name") + "</td></tr>";
                            else {
                                if (i == 0)
                                    conditionTable = "";
                                conditionTable += "<tr><td>" + a.getString(i) + "</td></tr>";
                            }
                        }
                        conditionTable += "</table>";
                    }
                    return conditionTable;
                } catch (Exception ex) {
                    return conditions;
                }
            } catch (Exception e) {
                return "Not specified";
            }

//                return jo.getString("conditions");
//            }catch(Exception e){return "Not specified";}
        }

        public String getNationality() {
            try {
                return jo.getString("nationality");
            } catch (Exception e) {
                return "";
            }
        }

        public String getNirc() {
            try {
                return jo.getString("nirc");
            } catch (Exception e) {
                return "";
            }
        }

        public String getDataByProvider() {
            try {
                String table = "";
                if (jo.has("dataByProvider")) {
                    JSONObject data = new JSONObject(jo.getString("dataByProvider"));
                    Iterator<String> keys = data.keys();

                    table = "<table class=\"mytable\" style=\"border-collapse: collapse; width:100%;margin: auto;\"  cellpadding=\"8\">\n";

                    while (keys.hasNext()) {
                        String key = keys.next();
                        if (!"".equals(data.getString(key))) {
                            String value = data.getString(key);
                            if (value.toLowerCase().startsWith("http")) {
                                value = "<a href=\"" + value + "\" target=\"_blank\">Click to view document</a>";
                            }
                            table += "<tr bgcolor=\"#26A69A\">\n"
                                    + "<td>" + key + "\n"
                                    + "</td>\n"
                                    + "\n"
                                    + "<td width=\"55%\">\n"
                                    + value + "\n"
                                    + "</td>\n"
                                    + "</tr>\n";
                        }
                    }
                    table += "</table>";
                }
                return table;
            } catch (Exception e) {
                return "";
            }
        }

    }
}
