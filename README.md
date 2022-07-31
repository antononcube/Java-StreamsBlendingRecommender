# Java-StreamsBlendingRecommender

[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)

Java implementation of a Streams Blending Recommender (SBR) framework.

Generally speaking, SBR is a "computer scientist" implementation of a recommendation system
based on sparse linear algebra. See the article
["Mapping Sparse Matrix Recommender to Streams Blending Recommender"](https://github.com/antononcube/MathematicaForPrediction/tree/master/Documentation/MappingSMRtoSBR),
[AA1], for detailed theoretical description of the data structures and operations with them.

This implementation is loosely based on the:

- Software monad
  ["MonadicSparseMatrixRecommender"](https://github.com/antononcube/MathematicaForPrediction/blob/master/MonadicProgramming/MonadicSparseMatrixRecommender.m), [AAp1],
  in Mathematica

- Software monad ["SMRMon-R"](https://github.com/antononcube/R-packages/tree/master/SMRMon-R), [AAp2], in R

- Object-Oriented Programming (OOP) implementation
  ["SparseMatrixRecommender"](https://pypi.org/project/SparseMatrixRecommender/), [AAp3], in Python

This implementation closely follows the:

- OOP implementation  ["ML-SparseMatrixRecommender"](https://github.com/antononcube/Raku-ML-StreamsBlendingRecommender), [AAp4], in Raku

- OOP implementation  ["SparseMatrixRecommender"](https://github.com/antononcube/Swift-StreamsBlendingRecommender), [AAp5], in Swift

Instead of "monads" the implementations in this package and [AAp3] use OOP classes.
Instead of "monadic pipelines" method chaining is used. 

--------

## References

### Articles

[AA1] Anton Antonov,
["Mapping Sparse Matrix Recommender to Streams Blending Recommender"](https://github.com/antononcube/MathematicaForPrediction/tree/master/Documentation/MappingSMRtoSBR),
(2019),
[GitHub/antononcube](https://github.com/antononcube).

### Packages, repositories

[AAp1] Anton Antonov,
[Monadic Sparse Matrix Recommender Mathematica package](https://github.com/antononcube/MathematicaForPrediction/blob/master/MonadicProgramming/MonadicSparseMatrixRecommender.m),
(2018),
[GitHub/antononcube](https://github.com/antononcube/).

[AAp2] Anton Antonov,
[Sparse Matrix Recommender Monad R package](https://github.com/antononcube/R-packages/tree/master/SMRMon-R),
(2018),
[R-packages at GitHub/antononcube](https://github.com/antononcube/R-packages).

[AAp3] Anton Antonov,
[SparseMatrixRecommender Python package](https://github.com/antononcube/Python-packages/tree/main/SparseMatrixRecommender),
(2021),
[Python-packages at GitHub/antononcube](https://github.com/antononcube/Python-packages).

[AAp4] Anton Antonov,
[ML::StreamsBlendingRecommender Raku package](https://github.com/antononcube/Raku-ML-StreamsBlendingRecommender),
(2021),
[GitHub/antononcube](https://github.com/antononcube).

[AAp5] Anton Antonov,
[StreamsBlendingRecommender Swift package](https://github.com/antononcube/Swift-StreamsBlendingRecommender),
(2022),
[GitHub/antononcube](https://github.com/antononcube).

