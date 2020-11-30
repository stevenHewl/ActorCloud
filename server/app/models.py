import os
import sys
from importlib import import_module
from actor_libs.utils import get_services_path
import importlib.util


# 不需要提前导入对象
def import_models():
    active_services = get_services_path()
    # for key, value in active_services.items():
    #     schemas_path = os.path.join(value, 'models.py')
    #     if not os.path.exists(schemas_path):
    #         continue
    #     service_path = '.'.join(value.partition('app')[-1].split('/'))
    #     service_models_path = 'app{0}.models'.format(service_path)
    #
    #     models_module = path_import(schemas_path)
    #     # models_module = import_module(service_models_path)
    #     service_models = models_module.__all__ if hasattr(models_module, '__all__') else []
    #     for name, attr in models_module.__dict__.items():
    #         if name in service_models:
    #             setattr(sys.modules[__name__], name, attr)


def path_import(absolute_path):
    spec = importlib.util.spec_from_file_location(absolute_path, absolute_path)
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return module


def check_module(module_name):
    module_spec = importlib.util.find_spec(module_name)
    if module_spec is None:
        print("Module: {} not found".format(module_name))
        return None
    else:
        print("Module: {} can be imported".format(module_name))
        return module_spec


import_models()




