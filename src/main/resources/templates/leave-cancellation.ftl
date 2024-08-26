<!DOCTYPE html>
<html>
<head>
    <title>Leave Cancellation Notification</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            color: #333;
            margin: 0;
            padding: 0;
        }

        .container {
            max-width: 600px;
            margin: 0 auto;
            padding: 20px;
            background-color: #f5f5f5;
            border-radius: 5px;
        }

        h1 {
            color: #333;
            font-size: 24px;
            margin-bottom: 20px;
        }

        p {
            font-size: 16px;
            line-height: 24px;
            margin-bottom: 20px;
        }

        table {
            border-collapse: collapse;
            width: 100%;
        }

        th, td {
            padding: 10px;
            text-align: left;
            border-bottom: 1px solid #ddd;
        }

        th {
            background-color: #f2f2f2;
            font-weight: bold;
        }

        .button {
            color: #fff;
            display: inline-block;
            padding: 10px 20px;
            text-decoration: none;
            border-radius: 4px;
            font-weight: bold;
            margin-right: 10px;
        }

        .button.cancel {
            background-color: #8c190d;
        }

        .button.cancel:hover {
            background-color: #6f1410;
        }

        .signature {
            margin-top: 45px;
            font-size: 16px;
        }

        .signature p {
            margin: 5px 0;
        }

        .support {
            font-size: 16px;
            margin-top: 20px;
        }

        .support a {
            color: #316fea;
            text-decoration: none;
        }
    </style>
</head>
<body>
    <div class="container">
        <h1>Leave Cancellation Notification</h1>
        <p>
                   Dear <span id="recipientRole">HR</span>,
                   <br>
                   I am writing to formally request the cancellation of the leave I had previously applied for. The details of my leave are as follows:
               </p>

        <table>
            <tr>
                <th>Applicant Name</th>
                <td><span id="Name">${Name}</span></td>
            </tr>
            <tr>
                <th>Leave Type</th>
                <td><span id="LeaveType">${LeaveType}</span></td>
            </tr>
            <tr>
                <th>Leave Dates</th>
                <td><span id="LeaveDates">${LeaveDates}</span></td>
            </tr>
            <tr>
                <th>Cancellation Reason</th>
                <td><span id="CancelReason">${CancelReason}</span></td>
            </tr>
        </table>

        <p>
            If you have any questions or concerns regarding this cancellation, please contact HR.
        </p>

        <a class="button cancel" href="${leaveCancelLink}" target="_blank" rel="noopener">Cancel</a>

        <div class="support">
            <p>
                Contact our support team if you have any questions or concerns.
                <br>
                <a href="mailto:teamhr.adt@gmail.com" target="_blank" rel="noopener">teamhr.adt@gmail.com</a>
            </p>
        </div>

        <div class="signature">
            <p>Best regards,</p>
            <p>Alphadot Technologies Team</p>
        </div>
    </div>
</body>
</html>
