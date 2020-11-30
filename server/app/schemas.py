import os
import sys
from importlib import import_module
from actor_libs.utils import get_services_path
import importlib.util


# 在linux服务器上可用
def import_schemas1():
    active_services = get_services_path()
    for key, value in active_services.items():
        schemas_path = os.path.join(value, 'schemas.py')
        if not os.path.exists(schemas_path):
            continue
        service_path = '.'.join(value.partition('app')[-1].split('/'))
        service_schemas_path = 'app{0}.schemas'.format(service_path)
        schemas_module = import_module(service_schemas_path)
        service_schemas = schemas_module.__all__ if hasattr(schemas_module, '__all__') else []
        for name, attr in schemas_module.__dict__.items():
            if name in service_schemas:
                setattr(sys.modules[__name__], name, attr)


# 在windows开发环境可用
def import_schemas():
    active_services = get_services_path()
    for key, value in active_services.items():
        schemas_path = os.path.join(value, 'schemas.py')
        if not os.path.exists(schemas_path):
            continue
        schemas_module = path_import(schemas_path)
        service_schemas = schemas_module.__all__ if hasattr(schemas_module, '__all__') else []
        for name, attr in schemas_module.__dict__.items():
            if name in service_schemas:
                setattr(sys.modules[__name__], name, attr)


def path_import(absolute_path):
    spec = importlib.util.spec_from_file_location(absolute_path, absolute_path)
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return module


import_schemas()
