ALTER TABLE quiz RENAME COLUMN id_quiz TO idQuiz;
ALTER TABLE quiz RENAME COLUMN created_by TO createdBy;

ALTER TABLE question RENAME COLUMN id_question TO idQuestion;
ALTER TABLE question RENAME COLUMN id_quiz TO idQuiz;
ALTER TABLE question RENAME COLUMN quiz_id TO idQuiz;

ALTER TABLE reponse RENAME COLUMN id_reponse TO idReponse;
ALTER TABLE reponse RENAME COLUMN id_question TO idQuestion;
ALTER TABLE reponse RENAME COLUMN question_id TO idQuestion;
ALTER TABLE reponse RENAME COLUMN reponse_id TO idReponse;
