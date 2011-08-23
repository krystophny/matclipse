function r = input(varargin)

if nargin < 1
    error('MATLAB:minrhs','Not enough input arguments.'); 
end
if nargin > 2
    error('MATLAB:maxrhs','Too many input arguments.'); 
end
if ~ischar(varargin{1})
    error('MATLAB:input:NonCharInput','The first argument to INPUT must be a string.');
end
if nargin == 2 & ~strcmp(varargin{2},'s')
    error('MATLAB:input:UnknownOption','The second argument to INPUT must be ''s''.');
end

str = inputdlg(varargin{1},'INPUT',1);
str = str{1};
%if nargin == 1, try, r = eval(str); catch, r = []; end, end
if nargin == 1
    try
        r = evalin('caller',str); 
    catch
        if isempty(str)
            r = [];
        else
            errid = 'MATLAB:UndefinedFunction';
            errmsg = ['Undefined function or variable ''',str,'''.'];
            error(errid,errmsg)
        end
    end
end
if nargin == 2 
    try
        r = str; 
    end
    if isempty(r), r = ''; end
end


%   message = 'INPUT_REQUEST#';
%   fname = 'INPUT_REQUEST_FILE';
%   tlim  = 10;  
%   
%   fprintf(1,'%s\n',[message,varargin{1}]);
%   fid = -1;
%   t0 = clock;
%   while fid == -1   
%     if etime(clock,t0) > tlim
%         if nargin == 1, r = []; else, r = ''; end 
%         return
%     end
%     fid = fopen(fname,'r');
%   end
%   tline = fgetl(fid);
%   if nargin == 1, try, r = eval(tline); catch, r = []; end, end
%   if nargin == 2, try, r = tline;       catch, r = ''; end, end
%   fclose(fid);
%   delete(fname);
