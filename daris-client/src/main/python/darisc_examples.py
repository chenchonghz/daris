# import sys
# sys.path.append('/path/to/darisc.py')
import darisc

def create_derived_dataset(study, type=None, name=None, description=None, input_file_path=None):  # @ReservedAssignment
    """ Creates/Uploads a dataset in the specified study. 
    """
    w = darisc.XmlStringWriter('args')
    w.add('pid', study)
    if name:
        w.add('name', name)
    if description:
        w.add('description', description)
    if input_file_path:
        w.add('in', input_file_path)
    session = darisc.Session(host='localhost', port='80', transport='http', token='LFo8gBlWYebrZYHsLeAolfLyvfnFxh623KChgeaErc5DFFKEb2tUMV14oZAk532kWNVjnsRiNEzIqcvJlTn1Dgpnp29qZ44nucMTbpYsTmWNPQEeOxhM5DDrIO3njMt39331')
    result = session.execute('om.pssd.dataset.derivation.create', w.doc_text())
    return result.value('id')  # returns dataset id

if __name__ == '__main__':
    # darisc.set_daris_client_jar_path('/path/to/daris-client.jar')
    create_derived_dataset(study='39.1.2.1.1.2', type='nifti/series', name='dataset1', input_file_path='/tmp/1.nii.gz')