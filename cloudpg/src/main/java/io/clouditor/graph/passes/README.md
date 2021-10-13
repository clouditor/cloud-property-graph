# Developing Passes

Passes are extensions of the Cloud Property Graph that can create and modify nodes and edges.
Passes can be language- or framework-specific, for example to create a LogOutput node when a specific logging library is used.
The goal of passes is to abstract away language specifics and other details, allowing to write simple, generic, reusable queries

Some implementation details need to be considered when writing passes:
- New passes need to be registered in App.kt (registerPass() in the doTranslate() method)
- Passes must overwrite the accept and cleanup methods
- Newly created nodes need to be added to the translationResult (t += n)
- Add a DFG edge via node1.addNextDFG(node2)
- Add an EOG edge via node1.addNextEOG(PropertyEdge(node1, node2))