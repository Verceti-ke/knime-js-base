/* eslint-env jquery */
/* global checkMissingData:false, callUpdate:false*/
/*
 * ------------------------------------------------------------------------
 *  Copyright by KNIME AG, Zurich, Switzerland
 *  Website: http://www.knime.com; Email: contact@knime.com
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License, Version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses>.
 *
 *  Additional permission under GNU GPL version 3 section 7:
 *
 *  KNIME interoperates with ECLIPSE solely via ECLIPSE's plug-in APIs.
 *  Hence, KNIME and ECLIPSE are both independent programs and are not
 *  derived from each other. Should, however, the interpretation of the
 *  GNU GPL Version 3 ("License") under any applicable laws result in
 *  KNIME and ECLIPSE being a combined program, KNIME AG herewith grants
 *  you the additional permission to use and propagate KNIME together with
 *  ECLIPSE with only the license terms in place for ECLIPSE applying to
 *  ECLIPSE and the GNU GPL Version 3 applying for KNIME, provided the
 *  license terms of ECLIPSE themselves allow for the respective use and
 *  propagation of ECLIPSE together with KNIME.
 *
 *  Additional permission relating to nodes for KNIME that extend the Node
 *  Extension (and in particular that are based on subclasses of NodeModel,
 *  NodeDialog, and NodeView) and that only interoperate with KNIME through
 *  standard APIs ("Nodes"):
 *  Nodes are deemed to be separate and independent programs and to not be
 *  covered works.  Notwithstanding anything to the contrary in the
 *  License, the License does not apply to Nodes, you are not required to
 *  license Nodes under the License, and you are granted a license to
 *  prepare and propagate Nodes, in each case even if such Nodes are
 *  propagated with or for interoperation with KNIME.  The owner of a Node
 *  may freely choose the license terms applicable to such Node, including
 *  when such Node is propagated with or for interoperation with KNIME.
 * ------------------------------------------------------------------------
 *
 * History
 *   May 29, 2019 (Daniel Bogenrieder, KNIME.com Gmbh, Konstanz, Germany): created
 */
window.knimeCredentialsWidget = (function () {
    var credentialsInput = {
        version: '2.0.0'
    };
    credentialsInput.name = 'KNIME Credentials Widget';
    var viewValid = false;
    var userInput,
        passwordInput,
        errorMessage,
        viewRepresentation;
    
    var enableInputFields = function (enable) {
        if (!viewRepresentation.noDisplay) {
            userInput.prop('disabled', !enable);
            passwordInput.prop('disabled', !enable);
        }
    };
    
    var displayServerCredentialsErrorMessage = function () {
        credentialsInput.setValidationErrorMessage('KNIME Server login credentials could not be fetched.');
    };
    
    credentialsInput.init = function (representation) {
        if (checkMissingData(representation)) {
            return;
        }
        viewRepresentation = representation;
        
        if (!knimeService.isRunningInWebportal()) {
            // display input fields if not running on server
            representation.noDisplay = false;
        }
        
        if (!representation.noDisplay) {
            var body = $('body');
            var qfdiv = $('<div class="quickformcontainer knime-qf-container">');
            body.append(qfdiv);
            qfdiv.attr('title', representation.description);
            qfdiv.append('<div class="label knime-qf-title">' + representation.label + '</div>');
            qfdiv.attr('aria-label', representation.label);

            if (representation.promptUsername) {
                var userLabel = $('<label class="knime-qf-label" style="display:block;" for="user_input">');
                userLabel.append('User');
                qfdiv.append(userLabel);
                userInput = $('<input id="user_input" type="text">');
                userInput.css('margin-bottom', '5px');
                userInput.attr('class', 'standard-sizing knime-qf-input knime-string knime-single-line');
                userInput.attr('aria-label', 'User');
                // user_input.width(400);
                var usernameValue = representation.currentValue.username;
                userInput.val(usernameValue);
                qfdiv.append(userInput);
                userInput.blur(callUpdate);
            }

            passwordInput = $('<input>');
            passwordInput.attr('id', 'pw_input');
            passwordInput.attr('type', 'password');
            passwordInput.attr('class', 'standard-sizing knime-qf-input knime-string knime-single-line');
            passwordInput.attr('aria-label', 'Password');
            // password_input.width(400);
            var passwordValue = representation.currentValue.password;
            passwordInput.val(passwordValue);
            var passwordLabel = $('<label class="knime-qf-label" style="display:block;" for="pw_input">');
            passwordLabel.append('Password');
            qfdiv.append(passwordLabel);
            qfdiv.append(passwordInput);

            errorMessage = $('<div class="knime-qf-error">');
            errorMessage.css('display', 'none');
            errorMessage.attr('role', 'alert');
            qfdiv.append(errorMessage);
            passwordInput.blur(callUpdate);
        }
        viewValid = true;
        
        if (representation.currentValue.disableServerCredentials) {
            displayServerCredentialsErrorMessage();
        }
        
        if (knimeService.pageBuilderPresent && !knimeService.isRunningInAPWrapper() &&
                representation.useServerLoginCredentials) {
            enableInputFields(false);
            viewValid = false;
            parent.KnimePageBuilderAPI.getUser().then(function (user) {
                viewValid = true;
                if (user) {
                    var viewValue = viewRepresentation.currentValue;
                    viewValue.username = user.userName;
                    viewValue.password = user.userPw;
                    if (!viewRepresentation.noDisplay) {
                        userInput.val(viewValue.username);
                        passwordInput.val(viewValue.password);
                    }
                } else {
                    displayServerCredentialsErrorMessage();
                }
                enableInputFields(true);
            }).catch(function () {
                viewValid = true;
                enableInputFields(true);
                displayServerCredentialsErrorMessage();
            });
        }
    };

    credentialsInput.validate = function () {
        if (!viewValid) {
            return false;
        }
        return true;
    };

    credentialsInput.setValidationErrorMessage = function (message) {
        if (!viewValid) {
            return;
        }
        if (message === null) {
            errorMessage.text('');
            errorMessage.css('display', 'none');
        } else if (viewRepresentation.noDisplay) {
            alert(message);
        } else {
            errorMessage.text(message);
            errorMessage.css('display', 'block');
        }
    };

    credentialsInput.value = function () {
        if (!viewValid) {
            return null;
        }
        var viewValue = viewRepresentation.currentValue;
        if (viewRepresentation.promptUsername && userInput) {
            viewValue.username = userInput.val();
        }
        if (passwordInput) {
            viewValue.password = passwordInput.val();
        }
        return viewValue;
    };

    return credentialsInput;

})();
