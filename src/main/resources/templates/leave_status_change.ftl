<!DOCTYPE html>
<html>
<head>
    <title>Leave Request Notification</title>
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
              background-color: #316fea;
              text-decoration: none;
              border-radius: 4px;
              font-weight: bold;
              margin-right: 10px;
          }

          .button.reject {
           color: #fff;
            background-color: #8c190d;
          }
          
           .button.approve {
            color: #fff;
            background-color: #088F8F;
          }

          .button:hover {
              background-color: #1e4bb5;
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
        <h1>Leave Request Notification</h1>
        <p>
            Dear <span id="Name">HR</span>,
            <br>
            I kindly request your approval for this leave request and would be happy to discuss this further if needed.
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
                <th>Leave Reason</th>
                <td><span id="Reason">${Reason}</span></td>
            </tr>
            <tr>
                <th>Status</th>
                <td><span id="Status">Pending</span></td>
            </tr>
        </table>

        <p>
            Please take the appropriate action on this leave request.
        </p>

        <a class="button approve" href="${leaveApprovalLink}" target="_blank" rel="noopener">Approve</a>
        <a class="button reject" href="${leaveRejectionLink}" target="_blank" rel="noopener">Reject</a>

        <div class="support">
            <p>
                Contact our support team if you have any questions or concerns.
                <br>
                <a href="javascript:void(0);" target="_blank" rel="noopener">teamhr.adt@gmail.com</a>
            </p>
        </div>

        <div class="signature">
            <p>Our best,</p>
            <p>Alphadot Technologies Team</p>
        </div>
    </div>
</body>
</html>
