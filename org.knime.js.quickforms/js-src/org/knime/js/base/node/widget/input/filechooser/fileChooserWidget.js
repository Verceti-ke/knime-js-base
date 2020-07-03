/* eslint-env jquery */
/* global checkMissingData:false */
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
 *   Jun 3, 2019 (Christian Albrecht, KNIME GmbH, Konstanz, Germany): created
 */
window.knimeFileChooserWidget = (function () {
    
    var fileChooser = {
        version: '2.0.0'
    };
    fileChooser.name = 'KNIME File Chooser Widget';
    var _representation = null;
    var _value = null;
    var _viewValid = false;
    var _container;
    var _errorMessage = null;
    
    var SCHEME = 'knime';
    var SCHEME_PART = SCHEME + '://';
    var WORKFLOW_RELATIVE = 'knime.workflow';
    var MOUNTPOINT_RELATIVE = 'knime.mountpoint';
    
    var SERVER_ITEM_TYPE = {
        WORKFLOW: 'Workflow',
        WORKFLOW_GROUP: 'WorkflowGroup',
        DATA: 'Data'
    };
    var WIDGET_ITEM_TYPE = {
        WORKFLOW: 'WORKFLOW',
        DIR: 'DIRECTORY',
        DATA: 'DATA',
        UNKNOWN: 'UNKNOWN'
    };
    
    // define startsWith function on strings
    if (!String.prototype.startsWith) {
        String.prototype.startsWith = function (searchString, position) { // eslint-disable-line no-extend-native
            position = position || 0;
            return this.substr(position, searchString.length) === searchString;
        };
    }
    
    var getNameFromPath = function (path) {
        var index = path.lastIndexOf('/');
        if (index + 1 >= path.length) {
            index = -1;
        }
        return index < 0 ? path : path.substring(index + 1);
    };
    
    var getTypeFromSelectedItem = function (node, path) {
        var childNodes;
        if (node) {
            childNodes = node.children;
        } else {
            childNodes = _representation.tree;
        }
        if (node && node.id === path) {
            return node.type;
        }
        for (var i = 0; i < childNodes.length; i++) {
            if (path.startsWith(childNodes[i].id)) {
                return getTypeFromSelectedItem(childNodes[i], path);
            }
        }
        return WIDGET_ITEM_TYPE.UNKNOWN;
    };
    
    var addEmptyRootItem = function (message) {
        var rootItem = {
            id: 'emptyTree',
            text: message + ' Please check your settings.',
            icon: null,
            state: {
                disabled: true,
                selected: false,
                opened: false
            }
        };
        _representation.tree = [rootItem];
    };
    
    var createTree = function () {
        _container.append('<div id="treeContainer" class="knime-qf-tree" aria-label="' + _representation.label + '">');
        $('#treeContainer').jstree({
            core: {
                data: _representation.tree,
                multiple: _representation.multipleSelection,
                worker: false
            }
        });

        $('#treeContainer').on('changed.jstree', function (e, data) {
            var selectedItems = [];
            for (var i = 0; i < data.selected.length; i++) {
                var path = data.selected[i];
                var type = getTypeFromSelectedItem(null, path);
                if (_representation.prefix) {
                    path = _representation.prefix + path;
                }
                var item = {
                    path: encodeURI(path),
                    type: type
                };
                selectedItems.push(item);
            }
            _value.items = selectedItems;
        });

        _container.append($('<br>'));
        _errorMessage = $('<span class="knime-qf-error">');
        _errorMessage.css('display', 'none');
        _errorMessage.attr('role', 'alert');
        _container.append(_errorMessage);

        _viewValid = true;
    };
    
    var createTreeItemRecursively = function (repositoryItem, defaultPaths) {
        if (!repositoryItem || !repositoryItem.path) {
            return null;
        }
        var treeItem = {
            id: repositoryItem.path,
            text: getNameFromPath(repositoryItem.path),
            state: {
                opened: false,
                disabled: false,
                selected: false
            },
            children: []
        };
        
        // set type and icons
        var baseUrl = knimeService.resourceBaseUrl + '/org/knime/js/base/node/widget/input/filechooser/img/';
        if (repositoryItem.type === SERVER_ITEM_TYPE.WORKFLOW) {
            if (!_representation.selectWorkflows) {
                return null;
            }
            treeItem.type = WIDGET_ITEM_TYPE.WORKFLOW;
            treeItem.icon = baseUrl + 'workflow.png';
        } else if (repositoryItem.type === SERVER_ITEM_TYPE.WORKFLOW_GROUP) {
            treeItem.type = WIDGET_ITEM_TYPE.DIR;
            treeItem.icon = baseUrl + 'workflowgroup.png';
            if (!_representation.selectDirectories) {
                treeItem.state.disabled = true;
            }
        } else if (repositoryItem.type === SERVER_ITEM_TYPE.DATA) {
            if (!_representation.selectDataFiles) {
                return null;
            }
            var fileBasePath = baseUrl + 'file-icons/';
            var endIndex = treeItem.text.lastIndexOf('.');
            var fileEnding = treeItem.name;
            if (endIndex > 0 && endIndex < treeItem.text.length - 1) {
                fileEnding = treeItem.text.substring(endIndex);
            }
            // TODO test for allowed file types
            
            treeItem.type = WIDGET_ITEM_TYPE.DATA;
            
            // set custom icon if possible
            var icon = fileBasePath + 'file.png';
            if (fileEnding) {
                switch (fileEnding) {
                case '.csv':
                    icon = fileBasePath + 'csv.png';
                    break;
                case '.json':
                    icon = fileBasePath + 'json.png';
                    break;
                case '.pmml':
                    icon = fileBasePath + 'pmml.png';
                    break;
                case '.table':
                    icon = fileBasePath + 'table.png';
                    break;
                case '.xls':
                    icon = fileBasePath + 'xls.png';
                    break;
                case '.xlsx':
                    icon = fileBasePath + 'xls.png';
                    break;
                case '.xml':
                    icon = fileBasePath + 'xml.png';
                    break;
                }
            }
            treeItem.icon = icon;
        }
        
        // set selection if default paths match
        if (defaultPaths && defaultPaths.length > 0) {
            defaultPaths.forEach(function (defaultPath) {
                if (defaultPath && defaultPath.startsWith(treeItem.id)) {
                    treeItem.state.opened = true;
                    if (defaultPath === treeItem.id) {
                        treeItem.state.opened = false;
                        treeItem.state.selected = true;
                    }
                }
            });
        }
        
        // resolve children
        if (repositoryItem.children && repositoryItem.children.length > 0) {
            repositoryItem.children.forEach(function (child) {
                var childItem = createTreeItemRecursively(child);
                if (childItem) {
                    treeItem.children.push(childItem);
                }
            });
        }
        
        // TODO remove if dir && no children && no dir can be selected
        
        return treeItem;
    };
    
    var setRepository = function (repository, rootPath, defaultPaths) {
        if (repository && repository.children && repository.children.length > 0) {
            var tree = [];
            repository.children.forEach(function (child) {
                var childItem = createTreeItemRecursively(child, defaultPaths);
                if (childItem) {
                    tree.push(childItem);
                }
            });
            if (tree.length > 0) {
                _representation.tree = tree;
            }
        } else {
            addEmptyRootItem('The root path ' + rootPath + ' could not be resolved or yielded an empty selection.');
        }
        createTree();
    };
    
    var setEmptyRepository = function (error) {
        if (!_representation.tree || _representation.tree.length < 1) {
            var errorText = 'No items found for selection. ';
            if (error) {
                errorText += error;
            }
            errorText += _representation.runningOnServer ? 'Check your settings.'
                : 'View selection only possible on server.';
            _representation.tree = [{
                id: 'emptyTree',
                text: errorText,
                icon: null,
                state: {
                    opened: false,
                    disabled: true,
                    selected: false
                },
                children: []
            }];
        }
        createTree();
    };

    fileChooser.init = function (representation) {
        if (checkMissingData(representation)) {
            return;
        }
        _representation = representation;
        _value = representation.currentValue;
        // erase default selection when running on server
        if (representation.runningOnServer) {
            _value.items = [];
        }
        
        var defaultPaths = [];
        var pathArray = _representation.currentValue.items;
        pathArray.forEach(function (defaultPath) {
            var path;
            if (typeof URL === 'function') {
                var url = new URL(defaultPath.path);
                path = url.pathname;
            } else {
                path = defaultPath.path;
            }
            if (path.substring(path.length - 1) === '/') {
                path = path.substring(0, path.length() - 1);
            }
            defaultPaths.push(path);
        });
        
        _container = $('<div class="quickformcontainer knime-qf-container">');
        $('body').append(_container);

        _container.attr('title', representation.description);
        _container.attr('aria-label', representation.label);
        _container.append('<div class="label knime-qf-title">' + representation.label + '</div>');
        
        var rootPath = _representation.rootDir || '/';
        // TODO resolve workflow and mountpoint-relative paths
        
        try {
            var request = parent.KnimePageBuilderAPI.getRepository({ path: rootPath, filter: null });
            if (request) {
                request.then(function (repo) {
                    setRepository(repo.response, rootPath, defaultPaths);
                }).catch(function (e) {
                    setEmptyRepository(e);
                });
            } else {
                setEmptyRepository();
            }
        } catch (e) {
            setEmptyRepository(e);
        }
        
        //TODO calls for legacy WebPortal
    };

    fileChooser.validate = function () {
        if (!_viewValid) {
            return false;
        }
        if (_value.items && _value.items.length > 0 && _value.items[0].path) {
            return true;
        } else {
            fileChooser.setValidationErrorMessage('Select at least one item to proceed.');
            return false;
        }
    };

    fileChooser.setValidationErrorMessage = function (message) {
        if (!_viewValid) {
            return;
        }
        if (message === null) {
            _errorMessage.text('');
            _errorMessage.css('display', 'none');
        } else {
            _errorMessage.text(message);
            _errorMessage.css('display', 'inline');
        }
    };

    fileChooser.value = function () {
        if (!_viewValid) {
            return null;
        }
        return _value;
    };

    return fileChooser;
})();
