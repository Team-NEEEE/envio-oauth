package io.envio.auth.domain.cli.view;

import org.springframework.stereotype.Component;

@Component
public class CliAuthRedirectView {

	public String success() {
		return """
			<html>
				<body style="text-align:center; padding-top:50px; font-family:sans-serif;">
					<h1 style="color:#28a745;">Authentication complete</h1>
					<p>GitHub login has been completed.</p>
					<p><strong>You can close this browser window and return to the CLI.</strong></p>
				</body>
			</html>
			""";
	}

	public String failure() {
		return """
			<html>
				<body style="text-align:center; padding-top:50px; font-family:sans-serif;">
					<h1 style="color:#dc3545;">Authentication failed</h1>
					<p>GitHub login could not be completed.</p>
					<p><strong>Please return to the CLI and try again.</strong></p>
				</body>
			</html>
			""";
	}
}
