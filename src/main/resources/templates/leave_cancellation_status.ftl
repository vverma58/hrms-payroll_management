<!DOCTYPE html>
<html>
<head>
    <title>Leave Cancellation Request Status Notification</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            color: #333;
            margin: 0;
            padding: 0;
            background-color: #e9ecef; /* Light background for the whole email */
        }

        .container {
            max-width: 600px;
            margin: 30px auto;
            padding: 20px;
            background-color: #ffffff; /* White background for the email content */
            border-radius: 8px;
            box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1); /* Subtle shadow effect */
        }

        h1 {
            color: #0056b3; /* Dark blue color for the heading */
            font-size: 24px;
            margin-bottom: 20px;
            text-align: center; /* Center-align heading */
        }

        p {
            font-size: 16px; /* Adjusted font size for readability */
            line-height: 1.6;
            margin-bottom: 20px;
        }

        table {
            border-collapse: collapse;
            width: 100%;
            margin-bottom: 20px; /* Added margin for spacing */
        }

        th, td {
            padding: 12px; /* Increased padding for better spacing */
            text-align: left;
            border-bottom: 1px solid #ddd;
        }

        th {
            background-color: #f2f2f2; /* Light grey for table header */
            font-weight: bold;
        }

        .button {
            color: #ffffff;
            display: inline-block;
            padding: 10px 20px;
            background-color: #316fea; /* Blue button color */
            text-decoration: none;
            border-radius: 4px;
            font-weight: bold;
            margin-right: 10px;
        }

        .button:hover {
            background-color: #1e4bb5; /* Darker blue for hover effect */
        }

        .signature {
            margin-top: 30px; /* Adjusted margin */
            font-size: 14px; /* Slightly smaller font size for the signature */
            color: #555; /* Dark grey color for the signature */
            text-align: center; /* Center-align signature text */
        }

        .signature p {
            margin: 5px 0;
        }

        .support {
            font-size: 14px;
            margin-top: 20px;
            text-align: center; /* Center-align support text */
        }

        .support a {
            color: #316fea; /* Blue color for support links */
            text-decoration: none;
        }

        .support a:hover {
            text-decoration: underline; /* Underline on hover for links */
        }
    </style>
</head>
<body>
    <div class="container">
        <h1>Leave Cancellation Request Status Notification</h1>
        <p>
            Dear <span id="Name">${Name}</span>,
            <br><br>
            <span id="Message">${Message}</span>
        </p>

        <table>
            <tr>
                <th>Leave Type</th>
                <td><span id="LeaveType">${LeaveType}</span></td>
            </tr>
            <tr>
                <th>Leave Dates</th>
                <td><span id="LeaveDates">${LeaveDates}</span></td>
            </tr>
            <tr>
                <th>Cancel Reason</th>
                <td><span id="CancelReason">${CancelReason}</span></td>
            </tr>
            <tr>
                <th>Status</th>
                <td><span id="Status">${Status}</span></td>
            </tr>
        </table>

        <div class="support">
            <p>
                Contact our support team if you have any questions or concerns.
                <br>
                <a href="mailto:teamhr.adt@gmail.com">teamhr.adt@gmail.com</a>
            </p>
        </div>

        <div class="signature">
            <p>Best regards,</p>
            <p>Alphadot Technologies Team</p>
        </div>
    </div>
</body>
</html>
